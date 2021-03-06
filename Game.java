package Evolution;

import java.util.ArrayList;
import java.util.LinkedList;

import cs015.fnl.EvolutionSupport.CS15NetworkVisualizer;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 *This class models the Evolution game, and is responsible for the majority of animation and user input.
 */
public class Game {
	private Pane _gamepane;
	private Pane _bottompane;
	private LinkedList<Pipe> _pipelist;
	private Pipe _initialpipe;
	private Timeline _timeline;
	private Button _speed2x;
	private Button _speed3x;
	private Population _population;
	private double _timealive;
	private ImageView _t1;
	private ImageView _t2;
	private double _currentgen;
	private Label _currentgenlabel;
	private Label _birdsalivelabel;
	private Label _bestfitnesslabel;
	private Label _avgfitnesslabel;
	private double _fitnesssum;
	private ArrayList<Integer> _generationlist;
	private ArrayList<Integer> _fitnesshistory;
	
	/*
	 * This method constructs Evolution, which contains the Population of birds, as well as the LinkedList of pipes.
	 */
	public Game() {
		_population = new Population();
		_currentgen= 1;
		_generationlist = new ArrayList<Integer>();
		_fitnesshistory= new ArrayList<Integer>();
		_gamepane = new Pane();
		_gamepane.setPrefSize(Constants.GAME_WIDTH, Constants.GAME_HEIGHT);
		_gamepane.setStyle("-fx-background-color: #000000;");
		this.generateTiles();
		this.setupGame();
		
		_bottompane = new Pane();
		_bottompane.setPrefSize(Constants.BOTTOM_WIDTH, Constants.BOTTOM_HEIGHT);
		_bottompane.setStyle("-fx-background-color: #000000;");

		this.setupTimeline();
		_gamepane.setOnKeyPressed(new KeyHandler());
		_gamepane.setFocusTraversable(true);
		this.speedButton();
		_speed2x.setOnAction(new ClickHandler());
		_speed3x.setOnAction(new ClickHandler());
	

	}
	
	/*
	 * This method sets up the game by initializing a linked list that contains all
	 * the pipes, as well as initializing the first pipe, setting its location,
	 * adding it graphically +logically to the pipelist.
	 */
	private void setupGame() {
		
		this.setupCurrentGen();
		
		this.setupBestFitness();
		this.setupAvgFitness();
	
		_population.createBirds(_gamepane);
		_pipelist = new LinkedList<Pipe>();
		_initialpipe = new Pipe();
		_initialpipe.getbottomPipe().setX(400);
		_initialpipe.gettopPipe().setX(400);
		_pipelist.add(_initialpipe);
		_gamepane.getChildren().addAll(_initialpipe.getbottomPipe(), _initialpipe.gettopPipe());
			
		
		
		/*
		 * this changes the mutation rate of the generation based on the fitness of the best bird of the last generation. I ended up not using this because it wasn't as efficient, but I wanted to leave
		 *it commented out as evidence of my experimentation with optimization.
		 */
		
		/*_mutationrate=0;
		if (_timealive>1000) {
			
			_mutationrate=.2;
		}
		if (_timealive<=1000 && _fitnesssum/50>600 ) {
		
			_mutationrate=.3;
		}
		
		if (_fitnesssum/50<=600) {
		
			_mutationrate=.4;
		}*/
		
		/*
		 *this changes the weights of the birds in the new population to the weights of the birds in the last generation with the highest fitness(lifespan)
		 *since setupgame is both called in the constructor and restart, the if statement is used to avoid a nullpointer 
		 *exception in the case where there are no deadbirds yet (the first time the game is played)
		 */
		if (_population.getDeadBirds().size() > 0) {
			
			
				
			for (int i = 0; i < (_population.getLiveBirds().size()*Constants.MIDDLEFRACTION); i++) {
				_population.getLiveBirds().get(i).setWeight(_population.getDeadBirds().peek().getsyn0Weight(), _population.getDeadBirds().peek().getsyn1Weight());
			_population.getLiveBirds().get(i).mutate(Constants.MUTATION_RATE);
			
			}
			
			
			_population.getDeadBirds().pop();


			for ( int i= (int) ( _population.getLiveBirds().size() * Constants.MIDDLEFRACTION); i<_population.getLiveBirds().size(); i++) {
				_population.getLiveBirds().get(i).setWeight(_population.getDeadBirds().peek().getsyn0Weight(), _population.getDeadBirds().peek().getsyn1Weight());
		_population.getLiveBirds().get(i).mutate(Constants.MUTATION_RATE);
		
		
				
			}
			}
			
			_population.getDeadBirds().clear();
			
			_fitnesssum=0; 
		}
		
	
	
	/*
	 *This method creates the buttons that can be clicked to change the speeds of the game
	 */
	private void speedButton() {
		_speed2x = new Button("2x");
		_bottompane.getChildren().add(_speed2x);

		_speed2x.setFocusTraversable(false);

		_speed3x = new Button("10x");
		_bottompane.getChildren().add(_speed3x);
		_speed3x.setLayoutX(50);
		_speed3x.setFocusTraversable(false);

	}
	
	/*
	 *This method sets up the currentgeneration label
	 */
	private void setupCurrentGen() {
		_currentgenlabel= new Label("Current generation:"+_currentgen);
		
		_currentgenlabel.setTextFill(Color.WHITE);
		_gamepane.getChildren().add(_currentgenlabel);
	}
	
	/*
	 *this method sets up the number of birds alive label
	 */
	private void setupNumberBirdsAlive() {
		_birdsalivelabel = new Label("Birds alive:"+_population.getLiveBirds().size());
		_birdsalivelabel.setTextFill(Color.WHITE);
		_birdsalivelabel.setLayoutY(15);
		_gamepane.getChildren().add(_birdsalivelabel);
	}
	
	/*
	 *This method sets up the best fitness label
	 */
	private void setupBestFitness() {
		_bestfitnesslabel = new Label("Best Fitness in Last Generation:"+_timealive);
		_bestfitnesslabel .setTextFill(Color.WHITE);
		_bestfitnesslabel .setLayoutY(30);
		_gamepane.getChildren().add(_bestfitnesslabel );
	}
	
	/*
	 *This method sets up the avg fitness label
	 */
	private void setupAvgFitness() {
		_avgfitnesslabel = new Label("Average Fitness in Last Generation:"+(_fitnesssum/50));
		
		_avgfitnesslabel .setTextFill(Color.WHITE);
		_avgfitnesslabel .setLayoutY(45);
		_gamepane.getChildren().add(_avgfitnesslabel);
	}
	/*
	 * This method sets up the timeline, and initiates the timehandler for each keyframe.
	 */
	private void setupTimeline() {
		
		KeyFrame kf = new KeyFrame(Duration.seconds(Constants.DURATION), new TimeHandler());

		_timeline = new Timeline(kf);
		_timeline.setCycleCount(Animation.INDEFINITE);
		_timeline.play();

	}

	private class TimeHandler implements EventHandler<ActionEvent> {

		/*
		 * This method constructs a TimeHandler.
		 */
		public TimeHandler() {
			
		}

		/*
		 * This method determines the changes that occur when the Timeline is initiated.
		 */
		public void handle(ActionEvent event) {
			// increments timealive counter by 1 every time the handle method is called

			_timealive = _timealive + 1;

			Game.this.checkdeadbird();
			Game.this.restart();
			// updates bird position and sets new values for inputs
			for (int i = 0; i < _population.getLiveBirds().size(); i++) {
				_population.getLiveBirds().get(i).updateBird();
				_population.getLiveBirds().get(i).setinputs(_pipelist, _initialpipe);
			}
			// scrolls pipes
			Game.this.scrollPipes();
			Game.this.scrollBackground();
			// calls tick, which in turn calls decidetojump (in bird class)
			Game.this.tick();

		}

	}

	/*
	 * This method creates a new pipe, and adds it to the pipelist
	 */
	private void generatePipes() {
		Pipe pipe = new Pipe();
		double ymax = Constants.TOPPIPE_Y + Constants.RANDOM_CONSTRAINT;
		double ymin = Constants.TOPPIPE_Y - Constants.RANDOM_CONSTRAINT;
		double randomy = ymin + (double) ((ymax - ymin + 1)) * Math.random();
		pipe.gettopPipe().setY(randomy);
		pipe.gettopPipe().setX(_pipelist.getLast().gettopPipe().getX() + Constants.PIPE_XSPACING);
		pipe.getbottomPipe().setY(randomy +Constants.PIPE_SPACE);
		pipe.getbottomPipe().setX(_pipelist.getLast().getbottomPipe().getX() + Constants.PIPE_XSPACING);
		_gamepane.getChildren().addAll(pipe.gettopPipe(), pipe.getbottomPipe());
		_pipelist.offer(pipe);
	}
	
	

	/*
	 * This method scrolls the pipes, and graphically removes a pipe from the
	 * pane/logically removes it from the pipelist if it moves off the screen
	 */
	private void scrollPipes() {

		if (_pipelist.getLast().getbottomPipe().getX() > 450 - (Constants.PIPE_XSPACING)) {
			for (int i = 0; i < _pipelist.size(); i++) {
				_pipelist.get(i).getbottomPipe()
						.setX(_pipelist.get(i).getbottomPipe().getX() - Constants.PIPE_MOVEMENT);
				_pipelist.get(i).gettopPipe().setX(_pipelist.get(i).gettopPipe().getX() - Constants.PIPE_MOVEMENT);

				if (_pipelist.getFirst().getbottomPipe().getX() <= -(Constants.PIPE_XSPACING + Constants.PIPE_WIDTH)) {

					_pipelist.remove(i);
					_gamepane.getChildren().removeAll(_pipelist.get(i).getbottomPipe(), _pipelist.get(i).gettopPipe());
				}
			}
		}

		else {
			this.generatePipes();
		}
	}
	
	/*
	 *This method creates the tiles for the background 
	 */
	private void generateTiles() {
		
		Image tile1= new Image (this.getClass().getResourceAsStream("background.png"));
		//setting up first tile
		_t1= new ImageView();
		_t1.setImage(tile1);
		_t1.setFitWidth(Constants.GAME_WIDTH);
		_t1.setFitHeight(Constants.GAME_HEIGHT);
		_t1.setX(0);
		
		//setting up second tile (made up of same image)
		_t2= new ImageView();
		_t2.setImage(tile1);
		_t2.setFitWidth(Constants.GAME_WIDTH);
		_t2.setFitHeight(Constants.GAME_HEIGHT);
		_t2.setX(Constants.GAME_WIDTH);
		
		_gamepane.getChildren().addAll(_t1, _t2);
		
	}
	
	/*
	 *This method scrolls the tiles in the background
	 */
	private void scrollBackground() {
	
		_t1.setX(_t1.getX()-Constants.PIPE_MOVEMENT);
		_t2.setX(_t2.getX()-Constants.PIPE_MOVEMENT);
	
		if (_t1.getX()==0) {
			_t2.setX(Constants.GAME_WIDTH-2);
		}
			
		if (_t2.getX()==0) {
			_t1.setX(Constants.GAME_WIDTH-2);
		}
	}
	
	/*
	 * Once a bird dies, this method graphically and logically removes it from the
	 * pane and list of livebirds and adds it to the stack of dead birds
	 */
	private void checkdeadbird() {
		// for the case when there is only one pipe (the nearest pipe is not the second
		// pipe)
		double topx1 = _initialpipe.gettopPipe().getX();
		double topy1 = _initialpipe.gettopPipe().getY();
		double bottomx1 = _initialpipe.getbottomPipe().getX();
		double bottomy1 = _initialpipe.getbottomPipe().getY();

		for (int i = _population.getLiveBirds().size() - 1; i >= 0; i--) {
			if (_population.getLiveBirds().get(i).getBirdshape().intersects(topx1, topy1, Constants.PIPE_WIDTH,
					Constants.PIPE_HEIGHT)
					|| _population.getLiveBirds().get(i).getBirdshape().intersects(bottomx1, bottomy1,
							Constants.PIPE_WIDTH, Constants.PIPE_HEIGHT)
					|| _population.getLiveBirds().get(i).getBirdshape().getY() < 0
					|| _population.getLiveBirds().get(i).getBirdshape().getY() > Constants.GAME_HEIGHT) {
				// logically and graphically removes bird if it dies
				
				_gamepane.getChildren().removeAll(_population.getLiveBirds().get(i).getBirdshape(),_birdsalivelabel);
				_population.getDeadBirds().add(_population.getLiveBirds().get(i));
				
				_population.getLiveBirds().remove(i);
				//adds the timealive of the deadbird to fitnesssum whenever a bird dies
				_fitnesssum=_fitnesssum +_timealive;

				this.setupNumberBirdsAlive();
			}
		}

		// for the cases where there is more than one pipe (nearest pipe is second pipe)
		if (_pipelist.size() > 1) {
			double topx = _pipelist.get(1).gettopPipe().getX();
			double topy = _pipelist.get(1).gettopPipe().getY();
			double bottomx = _pipelist.get(1).getbottomPipe().getX();
			double bottomy = _pipelist.get(1).getbottomPipe().getY();
			for (int i = _population.getLiveBirds().size() - 1; i >= 0; i--) {
				if (_population.getLiveBirds().get(i).getBirdshape().intersects(topx, topy, Constants.PIPE_WIDTH,Constants.PIPE_HEIGHT)
						|| _population.getLiveBirds().get(i).getBirdshape().intersects(bottomx, bottomy,Constants.PIPE_WIDTH, Constants.PIPE_HEIGHT)
						//killswitch implemented here
						|| _timealive>Constants.KILLSWITCH) {
					// logically and graphically removes bird if it dies
					
					_gamepane.getChildren().removeAll(_population.getLiveBirds().get(i).getBirdshape(),_birdsalivelabel);
					_population.getDeadBirds().add(_population.getLiveBirds().get(i));
					_population.getLiveBirds().remove(i);
					this.setupNumberBirdsAlive();
				}

			}

		}

	}

	/*
	 *This method clears pipes if there are no more livebirds, and calls the method setupgame.
	 */
	private void restart() {
		
		if (_population.getLiveBirds().size() == 0 ) {
			for (int i = 0; i < _pipelist.size(); i++) {
				_gamepane.getChildren().removeAll(_pipelist.get(i).getbottomPipe(), _pipelist.get(i).gettopPipe());

			}
			_pipelist.clear();
			_currentgen= _currentgen+1;
			
				_generationlist.add((int)_currentgen);
				
				_fitnesshistory.add((int)(_fitnesssum/(50)));
				
				
			//displays network visualizer when 50 generations have been created
			if (_currentgen==50) {
				CS15NetworkVisualizer visualizer= new CS15NetworkVisualizer(3,5);
			visualizer.plot(_generationlist, _fitnesshistory, "Average Fitness, Mutation Rate =.4, FINAL TRIAL2");
			}
			
			
			_gamepane.getChildren().removeAll(_currentgenlabel, _bestfitnesslabel, _avgfitnesslabel);
			
			Game.this.setupGame();
			//timealive counter is set to zero every time the game restarts
			_timealive = 0;
			
		}
		
	}
	


	/**
	 *This class models a ClickHandler that implements the interface EventHandler<ActionEvent>, and determines\
	 *what happens when the a button is clicked)
	 */
	private class ClickHandler implements EventHandler<ActionEvent> {
		/*
		 * This method determines the changes that occur when the button is clicked; in
		 * this case, the application is exited.
		 */
		public void handle(ActionEvent e) {
			Object button = e.getSource();
			if (button == _speed2x) {
				_timeline.setRate(2);
			}
			if (button == _speed3x) {
				_timeline.setRate(10);
			}
		}
	}

	/**
	 *This class models a KeyHandler that implements the interface EventHandler<ActionEvent>, and determines\
	 *what happens when the a certain key is pressed)
	 */
	 private class KeyHandler implements EventHandler<KeyEvent>{
	 
		/*
		 * This method constructs a KeyHandler.
		 */
		  public KeyHandler() {
		  
		  }
		  
		 // This method determines the changes that occur when a key is pressed.
		  
		 public void handle(KeyEvent e) { KeyCode keyPressed = e.getCode(); 
		 
		 //if statement moves the doodle left every time the spacebar is pressed. if
		  	if (keyPressed == KeyCode.SPACE) {
		  		//user input can move a bird in the gaame (which is the one that will live the longest)
		  		_population.getLiveBirds().get(0).setVelocity(Constants.REBOUND_VELOCITY); }
		  
		  
		  e.consume(); 
		  } 
		 }
	 
	 /*
	  * This method returns the Pane that contains the game, and was created so that it could be accessed in the PaneOrganizer class and added to the root BorderPane.
	  */
	public Pane getgamePane() {
		return _gamepane;
	}

	 /*
	  * This method returns the BottomPane that contains the buttons, and was created so that it could be accessed in the PaneOrganizer class and added to the root BorderPane.
	  */
	public Pane getbottomPane() {
		return _bottompane;
	}

	/*
	 * this method is called inside the handle method of Timehandler--it in turn calls decideToJump on the birds in the population of live birds every time
	 *the handle method is called.
	 */
	public void tick() {

		for (int i = 0; i < _population.getLiveBirds().size(); i++) {
			_population.getLiveBirds().get(i).decideToJump();
		}
	}

}
