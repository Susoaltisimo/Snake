package com.susoaltisimo;


import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

import java.util.Random;

public class Snake extends ApplicationAdapter {

	public static final int WIDTH = 160;
	public static final int HEIGHT = 120;

	private SpriteBatch batch;
	private SpriteBatch fontBatch;
	private Texture texture;
	private Pixmap pixmap;
	private Sprite sprite;

	private Array<Pos2D> snake = new Array<>();
	private Array<Pos2D> startSnake = new Array<>();
	private boolean left;
	private boolean right;
	private boolean up;
	private boolean down;

	private Array<Pos2D> objetivo = new Array<>();
	private boolean redibujar;
	private Random rnd = new Random();

	public static int SCORE;
	public static boolean GAMEOVER;
	//Don't clear the screen, just draw over it.
	public boolean rendered;
	//Only need this because libGDX works like that.
	public int renderCount;
	private BitmapFont font;

	@Override
	public void create () {
		OrthographicCamera cam = new OrthographicCamera();
		cam.setToOrtho(true, WIDTH, HEIGHT);
		OrthographicCamera fontCam = new OrthographicCamera();
		fontCam.setToOrtho(false, WIDTH, HEIGHT);
		batch = new SpriteBatch();
		batch.setProjectionMatrix(cam.combined);
		fontBatch = new SpriteBatch();
		fontBatch.setProjectionMatrix(fontCam.combined);
		pixmap = new Pixmap(WIDTH, HEIGHT, Format.RGBA8888);
		texture = new Texture(pixmap);
		sprite = new Sprite(texture);
		font = new BitmapFont();

		Gdx.input.setInputProcessor(new Input(this));

		//Cargamos la serpiente inicial
		startSnake.add(new Pos2D(5,1));
		startSnake.add(new Pos2D(5,2));
		startSnake.add(new Pos2D(5,3));
		startSnake.add(new Pos2D(5,4));
		startSnake.add(new Pos2D(5,5));
		startSnake.add(new Pos2D(5,6));
		startSnake.add(new Pos2D(5,7));
		startSnake.add(new Pos2D(5,8));
		startSnake.add(new Pos2D(5,9));
		restart();
	}

	public void restart(){
		snake.clear();
		snake.addAll(startSnake);
		up = true;
		down = left = right = false;

		//Limpiamos la pantalla (color negro)
		pixmap.setColor(Color.BLACK);
		pixmap.fill();

		//Se dibuja la serpiente
		for (Pos2D p2: snake){
			pixmap.setColor(Color.GREEN);
			pixmap.drawPixel(p2.x, p2.y);
		}
		//Initialize a target.
		nuevaManzana();
	}

	public void nuevaManzana(){
		//Borramos la manzana anterior
		pixmap.setColor(Color.BLACK);
		for (Pos2D p3: objetivo){
			pixmap.drawPixel(p3.x, p3.y);
		}
		objetivo.clear();

		//Construimos una nueva manzana
		Pos2D puntoM = new Pos2D(rnd.nextInt(WIDTH - 5) + 1, rnd.nextInt(HEIGHT - 5) + 1);
		pixmap.setColor(Color.RED);
		objetivo.add(puntoM);
		//Aumentamos su tamaño
		objetivo.add(new Pos2D(puntoM.x + 1, puntoM.y));//derecha
		objetivo.add(new Pos2D(puntoM.x, puntoM.y + 1));//arriba
		objetivo.add(new Pos2D(puntoM.x + 1, puntoM.y + 1));//arriba y derecha
		//Y la redibujamos
		redibujar = true;
	}

	public void manejadorInputs(){
		if (Input.LEFT && !right){
			left = true;
			right = up= down = false;
		}
		if (Input.RIGHT && !left){
			right = true;
			left = up = down = false;
		}
		if (Input.UP && !down){
			up = true;
			left = right = down = false;
		}
		if (Input.DOWN && !up){
			down = true;
			left = right = up = false;
		}
	}

	public void update(){
		//Actualizamos la posición de la serpiente
		if (left){
			snake.add(new Pos2D(snake.get(snake.size - 1).x - 1, snake.get(snake.size - 1).y));
		}
		if (right){
			snake.add(new Pos2D(snake.get(snake.size - 1).x + 1, snake.get(snake.size - 1).y));
		}
		if (up){
			snake.add(new Pos2D(snake.get(snake.size - 1).x, snake.get(snake.size - 1).y + 1));
		}
		if (down){
			snake.add(new Pos2D(snake.get(snake.size - 1).x, snake.get(snake.size - 1).y - 1));
		}

		//Comprobamos si se se sale de la pantalla por cualquiera de los lados
		if (snake.get(snake.size - 1).x < 0 || snake.get(snake.size - 1).x > WIDTH
				|| snake.get(snake.size - 1).y < 0 || snake.get(snake.size - 1).y > HEIGHT){
			GAMEOVER = true; // se acaba la partida
		}
		//Comprobamos si choca consigo mismma
		for (int i = 0; i < snake.size - 1; i++){
			if (snake.get(snake.size - 1).x == snake.get(i).x
					&& snake.get(snake.size - 1).y == snake.get(i).y){
				GAMEOVER = true; //se acaba la partida
				break;
			}
		}
		//Comprueba si la serpiente pasa por encima de la manzana
			for (int i = 0; i < objetivo.size; i++){
			if (snake.get(snake.size - 1).x == objetivo.get(i).x && snake.get(snake.size - 1).y == objetivo.get(i).y){
				nuevaManzana();
				SCORE+=100;
				break;
			}
		}
	}

	@Override
	public void render () {
		if (GAMEOVER && !rendered){
			renderScore();
		}
		if (!GAMEOVER){
			Gdx.gl.glClearColor(0, 0, 0, 1);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

			manejadorInputs();
			update();

			//Redibuja la cabeza de la serpiente
			pixmap.setColor(Color.GREEN);
			pixmap.drawPixel(snake.get(snake.size - 1).x, snake.get(snake.size - 1).y);

			//Redibuja la cola si no se consigue la manzana.
			if (!redibujar){
				pixmap.setColor(Color.BLACK);
				pixmap.drawPixel(snake.get(0).x, snake.get(0).y);
				snake.removeIndex(0);
			}

			//Redibuja una nueva manzana si se necesita
			if (redibujar){
				for (Pos2D p4: objetivo){
					pixmap.setColor(Color.RED);
					pixmap.drawPixel(p4.x, p4.y);
				}
				redibujar = false;
			}

			texture.draw(pixmap, 0, 0);
			batch.begin();
			sprite.draw(batch);
			batch.end();
		}
	}

	public void renderScore(){
		fontBatch.begin();
		font.setColor(Color.BLUE);
		font.draw(fontBatch, "GAME OVER", 30, 105);
		font.draw(fontBatch, "SCORE: " + SCORE, 32, 75);
		font.draw(fontBatch, "Press SPACE", 30, 45);
		fontBatch.end();
		renderCount++;
		if (renderCount == 2){
			rendered = true;
		}
	}

	private static class Pos2D {
		private int x;
		private int y;

		public Pos2D(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}

	private static class Input extends InputAdapter {
		Snake snake;

		Input(Snake snake){
			this.snake = snake;
		}

		public static boolean LEFT;
		public static boolean RIGHT;
		public static boolean UP;
		public static boolean DOWN;

		public boolean keyDown(int k) {
			if (k == Keys.LEFT){
				LEFT = true;
				RIGHT = UP = DOWN = false;
			}
			if (k == Keys.RIGHT){
				RIGHT = true;
				LEFT = UP = DOWN = false;
			}
			if (k == Keys.UP){
				UP = true;
				RIGHT = LEFT = DOWN = false;
			}
			if (k == Keys.DOWN){
				DOWN = true;
				RIGHT = UP = LEFT = false;
			}
			if (k == Keys.SPACE && Snake.GAMEOVER){
				Snake.GAMEOVER = false;
				Snake.SCORE = 0;
				snake.rendered = false;
				snake.renderCount = 0;
				snake.restart();
			}
			return true;
		}

		public boolean keyUp(int k) {
			if (k == Keys.LEFT){
				LEFT = false;
			}
			if (k == Keys.RIGHT){
				RIGHT = false;
			}
			if (k == Keys.UP){
				UP = false;
			}
			if (k == Keys.DOWN){
				DOWN = false;
			}
			return true;
		}
	}
}

