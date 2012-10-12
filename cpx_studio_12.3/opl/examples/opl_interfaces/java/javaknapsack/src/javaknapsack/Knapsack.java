/*
* Licensed Materials - Property of IBM
* 5725-A06 5725-A29 5724-Y48 5724-Y49 5724-Y54 5724-Y55 
* Copyright IBM Corporation 1998, 2011. All Rights Reserved.
*
* Note to U.S. Government Users Restricted Rights:
* Use, duplication or disclosure restricted by GSA ADP Schedule
* Contract with IBM Corp.
*/ 

package javaknapsack;
import ilog.concert.*;
import ilog.opl.*;

// This is an simple Knapsack algorithm used to illustrate how to 
// call java functions from OPL.
// Refer to the description of the example cutstock_ext_main in the
// OPL documentation for more information

public class Knapsack 
{
    private int[] weights, solution;
    private double[] values;
 
    public Knapsack()    {}

    double solve(int target)
    {
        // V is the array of current value
        double V[] = new double [target+1];
        // W is the array stating which element to use
        int W[] = new int [target+1];
        
        // build the first column
        V[0] = 0;
        W[0] = -1;
                
        for (int j = 0; j <= target; j++) 
        {
            V[j] = 0;
            W[j] = -1;
        }
        
        int best = -1;
        double bestValue = 0;

        for (int j = 1; j <= target; j++) {
            for (int i = 1; i <= values.length; i++) {                                
                if (j - weights[i-1] >= 0) // if possible to add element 
                {
                    // pre-compute the column value
                    double tmp = values[i-1] + V[j-weights[i-1]];
                    if (V[j] < tmp) // if better solution.
                    {
                        V[j] = tmp;
                        W[j] = i-1;
                        if (tmp > bestValue) 
                        {                            
                            bestValue = tmp;
                            best = j;
                        }

                    }
                } // end else            
            } // end for i loop            
        } // end for j loop

        // Store the optimal solution (indicated by best) into the solution array
        int i = best;
        double solutionValue = V[i];
        solution = new int [weights.length];
        while (W[i]>=0) 
        {            
            solution[W[i]]++;
            i -= weights[W[i]];
        }
        return solutionValue;                
    } // end solve

    
    public void updateInputs(IloOplElement oplWeights, IloOplElement oplValues) 
    {
        try 
        {
            int nbItems = oplWeights.asIntMap().getSize();

            weights = new int[nbItems];
            values = new double[nbItems];

            for (int i = 0; i < nbItems; i++)
            {
                weights[i] = (int)oplWeights.asIntMap().get(1+i);  // indexing from 1 to nbItems in OPL
                values[i] = (double)oplValues.asNumMap().get(1+i);
            }

        } catch (IloOplException ex) {
            System.err.println("### OPL exception in Knapsack.updateInputs: " + ex.getMessage());
            ex.printStackTrace();
        } catch (IloException ex) {
            System.err.println("### CONCERT exception in Knapsack.updateInputs: " + ex.getMessage());
            ex.printStackTrace();
        } catch (Exception ex) {
            System.err.println("### UNEXPECTED UNKNOWN ERROR in Knapsack.updateInputs...");
            ex.printStackTrace();
        }

    } // end updateInputs

    public double solve(IloOplElement oplSolution, int size) 
    {        
        try 
        {
            double solutionValue  = solve(size);

            for (int i = 0; i < weights.length; i++)  
            {
                oplSolution.asIntMap().set(1+i, solution[i]);  // indexing from 1 to Nbitems in OPL
            }
            
            return solutionValue;
        } catch (IloOplException ex) {
            System.err.println("### OPL exception in Knapsack.solve: " + ex.getMessage());
            ex.printStackTrace();
        } catch (IloException ex) {
            System.err.println("### CONCERT exception in Knapsack.solve: " + ex.getMessage());
            ex.printStackTrace();
        } catch (Exception ex) {
            System.err.println("### UNEXPECTED UNKNOWN ERROR in Knapsack.solve...");
            ex.printStackTrace();
        }
        return 0;
    } // end solve
}
