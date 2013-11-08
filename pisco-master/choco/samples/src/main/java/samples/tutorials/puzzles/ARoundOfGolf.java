package samples.tutorials.puzzles;

import static choco.Choco.allDifferent;
import static choco.Choco.lt;
import static choco.Choco.makeIntVarArray;
import static choco.Choco.neq;
import static choco.Choco.*;

import java.util.logging.Level;

import samples.tutorials.PatternExample;
import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solution;
/**
 * 
 * From <a href="http://brownbuffalo.sourceforge.net/RoundOfGolfClues.html">Solve Logic Word Puzzles with CLP</a></br>
 * <ul>
 * <li> Title: A Round of Golf
 * <li> Author: Ellen K. Rodehorst
 * <li> Publication: Dell Favorite Logic Problems
 * <li> Issue: Summer, 2000
 * <li> Puzzle #: 9
 * <li> Stars: 1
 * </ul>
 *
 * Compare with the:
 * <ul>
 * <li> <a href="http://www.f1compiler.com/samples/A%20Round%20of%20Golf.f1.html">F1 model</a> 
 * <li> <a href="http://www.hakank.org/minizinc/a_round_of_golf.mzn">Minizinc model</a>
 * </ul>
 * <h3> A Round of Golf puzzle (Dell Logic Puzzles) in Choco</h3>
 *.
 * <p>When the Sunny Hills Country Club golf course isn't in use by club members, 
 * of course, it's open to the club's employees. Recently, Jack and three other 
 * workers at the golf course got together on their day off to play a round of 
 * eighteen holes of golf.
 * Afterward, all four, including Mr. Green, went to the clubhouse to total their scorecards. 
 * </br>
 * Each man works at a different job (one is a short-order cook), and each shot a different score in the game.
 * No one scored below 70 or above 85 strokes. 
 * From the clues below, can you discover each man's full name, job and golf score?
 * 
 * <ol>
 * <li> Bill, who is not the maintenance man, plays golf often and had the lowest score of the foursome.
 * <li>  Mr. Clubb, who isn't Paul, hit several balls into the woods and scored ten strokes more than the pro-shop clerk.
 * <li>  In some order, Frank and the caddy scored four and seven more strokes than Mr. Sands.
 * <li> Mr. Carter thought his score of 78 was one of his better games, even though Frank's score  was lower.
 * <li> None of the four scored exactly 81 strokes.
 * </ol>
 * 
 * <b>Determine</b>: First Name - Last Name - Job - Score 
 *
 * <h3> L'énigme de la partie de golf (Dell Logic Puzzles) en Choco</h3>
 *.
 * <p> Quand le terrain de golf du club des "collines ensoleillées" n'est pas utilisé par les membres du club, les employés y ont libre accès.
 * Récemment, Jack et trois autres employés du club de golf se sont retrouvés pendant leur jour de congé pour jouer un dix-huit trous.
 * À la fin, ils allèrent calculer leur score au club house tous les quatres, en comptant M. Green.
 * 
 * </br>
 * Les employés ont des emplois (l'un d'eux est cuistot) et des scores différents.
 * Ils ont tous obtenu un score compris entre 70 et 85 coups. 
 * D'après les indices suivants, est-ce que vous pouvez découvrir le nom complet, le travail et le score de chacun.
 * 
 * <ol>
 * <li> Bill, qui ne travaille pas à l'entretien (maintenance man), joue souvent au golf, mais a obtenu le meilleur (le plus bas) score des quatres. 
 * <li> M. Clubb, qui ne s'appelle pas Paul, a envoyé la balle les bois plus souvent que le commis du magasin (pro-shop cook) et compte finalement dix coups de plus.
 * <li> Frank et le caddie ont frappé quatre et sept coups (ou inversement) de plus que M. Sands.
 * <li> M. Carter pense qu'il a réalisé une de ses meilleures parties avec un score de 78 même si le score de Frank est meilleur.
 * <li> Aucun joueur n'a obtenu un score de 81.
 * </ol>
 * 
 * <b>Determiner</b>: Prénom - Nom de famille - Emploi - Score 
 * 
 * 
 * 
 * 
 * 
 *  @author Arnaud Malapert
 *
 *	@see {@link Choco#allDifferent(IntegerVariable...)} and {@link Choco#nth(IntegerVariable, IntegerVariable[], IntegerVariable)}
 */
public class ARoundOfGolf extends PatternExample {

	private final static int Jack=0, Bill=1, Paul=2, Frank=3;
	private final static int Green=0, Clubb=1, Sands=2, Carter=3;
	private final static int cook=0, maint=1, clerk=2, caddy=3;

	private final static String[][] DATA = {
		{"Jack", "Bill", "Paul", "Frank"},
		{"Green", "Clubb", "Sands","Carter"},
		{  "cook", "maint", "clerk","caddy"}
	};

	IntegerVariable[] scores, lastnames, jobs;

	public ARoundOfGolf() {
		super();
	}
	@Override
	public void printDescription() {
		 LOGGER.info("When the Sunny Hills Country Club golf course isn't in use by club members,"); 
		 LOGGER.info("of course, it's open to the club's employees. Recently, Jack and three other"); 
		 LOGGER.info("workers at the golf course got together on their day off to play a round of eighteen holes of golf."); 
		 LOGGER.info("Afterward, all four, including Mr. Green, went to the clubhouse to total their scorecards."); 
		 LOGGER.info("Each man works at a different job (one is a short-order cook), and each shot a different score in the game."); 
		 LOGGER.info("No one scored below 70 or above 85 strokes."); 
		 LOGGER.info("From the clues below, can you discover each man's full name, job and golf score?\n");
		 LOGGER.info("\t1. Bill, who is not the maintenance man, plays golf often and had the lowest score of the foursome.");
		 LOGGER.info("\t2. Mr. Clubb, who isn't Paul, hit several balls into the woods and scored ten strokes more than the pro-shop clerk.");
		 LOGGER.info("\t3. In some order, Frank and the caddy scored four and seven more strokes than Mr. Sands.");
		 LOGGER.info("\t4. Mr. Carter thought his score of 78 was one of his better games, even though Frank's score  was lower.");
		 LOGGER.info("\t5. None of the four scored exactly 81 strokes.\n");

	}

	@Override
	public void buildModel() {
		model = new CPModel();

		scores = makeIntVarArray("score", 4, 70, 85);
		lastnames= makeIntVarArray("name", 4,0,3);
		jobs = makeIntVarArray("job", 4,0,3);

		IntegerVariable[] lastnameScores = makeIntVarArray("name-score", 4, 70, 85);
		IntegerVariable[] jobScores = makeIntVarArray("job-score", 4, 70, 85);
		for (int i = 0; i < scores.length; i++) {
			model.addConstraints(
					nth(jobs[i], scores, jobScores[i]),
					nth(lastnames[i], scores, lastnameScores[i])
					);
		}

		/*
		 * Each man works at a different job (one is a short-order 
		 * cook), and each shot a different score in the game.
		 */
		model.addConstraints(
				allDifferent(lastnames),
				allDifferent(jobs),
				allDifferent(scores)
				);
		/*
		 * 1. Bill, who is not the maintenance man, plays golf often and had the lowest 
		 * score of the foursome.
		 */
		model.addConstraints(
				neq(Bill, jobs[maint]),
				lt(scores[Bill], scores[Paul]),
				lt(scores[Bill], scores[Jack]),
				lt(scores[Bill], scores[Frank])
				);

		/*	
		 * 2. Mr. Clubb, who isn't Paul, hit several balls into the woods and scored ten 
		 * strokes more than the pro-shop clerk.
		 */

		model.addConstraints(
				neq(Paul, lastnames[Clubb]),
				eq(lastnameScores[Clubb], plus(jobScores[clerk],10))
				);
		/*
		 *  3. In some order, Frank and the caddy scored four and seven more strokes than Mr. Sands.
		 */
		model.addConstraints(
				neq(Frank, jobs[caddy]),
				neq(Frank, lastnames[Sands]),
				neq(jobs[caddy], lastnames[Sands])
				);
		//		model.addConstraint(		
		//				or(
		//						and(
		//								eq( scores[Frank], plus(lastnameScores[Sands], 4)),
		//								eq( jobScores[caddy], plus(lastnameScores[Sands], 7))
		//								),
		//								and(
		//										eq( scores[Frank], plus(lastnameScores[Sands], 7)),
		//										eq( jobScores[caddy], plus(lastnameScores[Sands], 4))
		//										)
		//						)
		//				);
		IntegerVariable[] deltas = makeIntVarArray("delta", 2, new int[]{4,7});
		model.addConstraints(
				eq( scores[Frank], plus(lastnameScores[Sands], deltas[0])),
				eq( jobScores[caddy], plus(lastnameScores[Sands], deltas[1])),
				neq(deltas[0],deltas[1])
				);
		/*
		 * 4. Mr. Carter thought his score of 78 was one of his better games, even 
		 *    though Frank's score  was lower.
		 */
		model.addConstraints(
				neq(lastnames[Carter], Frank),
				eq(lastnameScores[Carter], 78),
				lt(scores[Frank], lastnameScores[Carter])
				);
		/* 5. None of the four scored exactly 81 strokes.*/
		for (int i = 0; i < scores.length; i++) {
			model.addConstraint(neq(scores[i],81));
		}
	}

	@Override
	public void buildSolver() {
		solver = new CPSolver();
		solver.read(model);
	}

	@Override
	public void prettyOut() {
		if(LOGGER.isLoggable(Level.INFO) && solver.existsSolution()) {
			//LOGGER.info(solver.solutionToString());
			String[][] results = new String[2][4];
			for (int i = 0; i < scores.length; i++) {
				results[0][solver.getVar(lastnames[i]).getVal()] = DATA[1][i];
				results[1][solver.getVar(jobs[i]).getVal()] = DATA[2][i];
			}
			LOGGER.info("Answer:");
			for (int i = 0; i < scores.length; i++) {
				LOGGER.log(Level.INFO, "{0} {1} works as a {2} and scored at {3}.", 
						new Object[]{DATA[0][i],results[0][i], results[1][i],solver.getVar(scores[i]).getVal()});
			}
			LOGGER.info("");
		}


	}

	@Override
	public void solve() {
		//		solver.generateSearchStrategy();
		//		solver.getSearchStrategy().initialPropagation();
		//		LOGGER.info(solver.pretty());
		solver.solveAll();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new ARoundOfGolf().execute(args);

	}

}
