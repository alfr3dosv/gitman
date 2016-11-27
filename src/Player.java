import java.util.*;
import java.io.*;

public class Player implements Runnable
{
	public int y;
	public int x;
	public volatile DIRECTION dir = DIRECTION.NONE;
	private volatile boolean alive;
	public volatile char key;
	public volatile STATE status = STATE.STATIC;
	Scanner console;
	RawConsoleInput in;
	//timers
	long start_time;// contador de tiempo entre teclas
	final int NEXT_KEY=20;//intervalo de tiempo entre teclas
	//jumping
	long jump_start=0;
	final int[] JUMP_WAIT={50,100,300};
	final int JUMP_WAIT_KEY=300;
	long jump_last_key;
	int jump_steps=1;
	int jump_step=0;
	//falling
	long fall_start=0;
    final int[] FALL_WAIT={100,500,100};
	int fall_step=0;
	int speed_x;

	public Player()
	{
		alive = true;
		x = 0; 
		y = 0;
		console = new Scanner(System.in);
		in = new RawConsoleInput();
	}
	public void run()
	{
		while(status != STATE.PAUSED)
		{
			captureInput();
			move();
			try{
				in.resetConsoleMode();
				System.out.flush();
			}
			catch (Exception e){
				e.printStackTrace();
			}
		}
		try{
			in.resetConsoleMode();
			console.next();
		}
		catch(Exception e){}
	}
	private void move()
	{
		if(status == STATE.JUMPING){
			jump();
		}	
		switch (dir)
		{
			case DOWN: 
				setY( (getY()+1));
			break;
			
			case UP:

					if( ((System.currentTimeMillis() - jump_last_key) < JUMP_WAIT_KEY) && (jump_steps < 2)){
						jump_steps++;
						jump_last_key = System.currentTimeMillis();
					}
			break;

			case LEFT: 
				setX(getX()-1);
				if(speed_x>0){
					speed_x = 0;
				}
				else if(speed_x>-3){
					speed_x--;
				}
			break;

			case RIGHT:
				setX( (getX()+1) );
				if(speed_x<0){
					speed_x = 0;
				}
				else if(speed_x<3){
					speed_x++;
				}
			break;
		}
		if(status == STATE.STATIC){
			speed_x = 0;
		}
	}
	public char captureKey()
	{
		StringBuffer str = new StringBuffer();
		char key = 'f';
		try {
			if( (System.currentTimeMillis() - start_time) > NEXT_KEY)
			{
				key = (char)in.read(false);
				start_time = System.currentTimeMillis();
			}	
		} 
		catch(IOException ex){key='f';}  
		finally
		{
			return key;
		}
	} 
	public void captureInput() 
	{
    	char keyCode = captureKey();
	    dir = DIRECTION.NONE;
	    switch( keyCode ) 
	    { 
	        case 'w':
	            dir = DIRECTION.UP;
	            status = STATE.JUMPING;
	            break;
	        case 's':
	            dir = DIRECTION.DOWN;
	            break;
	        case 'a':
	            dir = DIRECTION.LEFT;
	            status = STATE.WALKING;
	            break;
	        case 'd':
	            dir = DIRECTION.RIGHT;
	            status = STATE.WALKING;
	            break;
	        case ':':
	        	dir = DIRECTION.NONE;
	        	status = STATE.PAUSED;
	        	break;
	       	default:
	       		dir = DIRECTION.NONE;
	     }
	 }
	public char[][] image()
	{
		char[][] img = new char[3][3];
		char[] img_0 = {' ','o',' '};
		char[] img_1 = {'/','|','\\'};
		char[] img_2 = {'/',' ','\\'};
		img[0] = img_0;
		img[1] = img_1;
		img[2] = img_2;
		return img;
	}

	public enum DIRECTION {UP, DOWN, LEFT, RIGHT, NONE}
	public enum STATE {JUMPING, FALLING, WALKING, STATIC, PAUSED}
	public static class Image{
		static final int SIZE_X = 3;
		static final int SIZE_Y = 3;
	}

	public void collisions(char[][] frame)
	{
		/* drawArea
		 * area donde se dibuja el jugador
		 */
		char[][] drawArea = new char[Image.SIZE_Y][Image.SIZE_X];
		for(int pos_y=0; pos_y<Image.SIZE_Y; pos_y++)
		{
			for(int pos_x=0; pos_x<Image.SIZE_X; pos_x++)
			{
				drawArea[pos_y][pos_x] = frame[getY()+pos_y][getX()+pos_x];
			}	
		}
		/* enemies
		 * commprueba que el area donde se va dibujar no haya enemigos
		 * en caso de que si mata al jugador y pausa el juego
		 */
		for( char[] caracteres: drawArea)
		{
			for( char caracter:caracteres)
			{				
				if(caracter == '*')
				{
				killPlayer();
				status = STATE.PAUSED;
				}
			}
		}
		/* bottom
		 * si se encuentra un '-' debajo del drawArea hay piso
		 * logicamente no puede atravesar el piso, lo subimos
		 */
		if( getY() <= (frame.length+Image.SIZE_Y+1) )//evitamos out of ranges 
		{
			boolean PISO = false;
			for(int pos_x=0; pos_x<Image.SIZE_X; pos_x++)//area sobre el jugador
			{
				if( frame[getY()+2][getX()+pos_x] == '-')
				{
					setY(getY()-1);//sube
					status = STATE.STATIC;
					PISO = true;
					break;
				}
				if( frame[getY()+3][getX()+pos_x] == '-'){//area debajo del jugador
					PISO = true;
				}
			}
			if( (PISO == false) &&(status != STATE.JUMPING) && (getY()!=(frame.length)) && ((System.currentTimeMillis() - jump_last_key) > JUMP_WAIT_KEY) ) 
			{
				status = STATE.FALLING;
				fall(1);
			}
			
		}
	}
	public void jump()
	{	
	    if(jump_start == 0){
			jump_start = System.currentTimeMillis();
			jump_last_key = jump_start;
		}
		else if( (System.currentTimeMillis() - jump_start) > JUMP_WAIT[jump_step] ) 
		{
			setY(getY()-1);
			if((jump_step >= jump_steps)){
				status = STATE.FALLING;
				fall(jump_steps+1);	
				jump_steps=1;
				jump_step=-1;
				jump_start = 0;
			}
			if(speed_x != 0){
				if(speed_x>0){
					setX(getX()+1+jump_step);
					speed_x--;		
				}
				else if(speed_x<0){
					setX(getX()-1-jump_step);
					speed_x++;		
				}
			}
			jump_step++;
		}
		if(jump_steps == 0){
			status = STATE.FALLING;
		}
	}
	public void fall(int spaces)
	{
		while( (spaces > 0) && (status == STATE.FALLING) )
		{
		    if(fall_start == 0){
				fall_start = System.currentTimeMillis();
			}
			else if( (System.currentTimeMillis() - fall_start) > FALL_WAIT[fall_step] ) 
			{
				setY(getY()+1);
				if(fall_step<=2){
					fall_step++;
				}
				if(speed_x != 0){
					if(speed_x>0){
						setX(getX()+1);
					}
					else if(speed_x<0){
						setX(getX()-1);		
					}
				}	
				spaces--;
			}
		}
		fall_start = 0;
		fall_step = 0;
	}
	//setters y getters para x,y 
	public void setX(int x){
		if( (x < (Display.SIZE_X-Player.Image.SIZE_X)) && (x>=0) )
			this.x = x;
	}
	public void setY(int y){
		if( (y < (Display.SIZE_Y-Player.Image.SIZE_Y)) && (y>=0) )
			this.y = y;
	}
	public int getX(){return x;}
	public int getY(){return y;}
	
	//setters y getters para alive
	public boolean isAlive(){return this.alive;}
	private void killPlayer(){this.alive = false;}

}