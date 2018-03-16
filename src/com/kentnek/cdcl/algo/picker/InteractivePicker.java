package com.kentnek.cdcl.algo.picker;

import com.kentnek.cdcl.model.Assignment;

import java.util.Scanner;

/**
 * Receives user input for branch to pick.
 * <p>
 *
 * @author kentnek
 */

public class InteractivePicker implements BranchPicker {
    private Scanner reader = new Scanner(System.in);

    @Override
    public VariableValue select(Assignment assignment) {
        System.out.print("Enter next literal: ");
        int literal = reader.nextInt();
        return new VariableValue(Math.abs(literal), literal > 0);
    }
}
