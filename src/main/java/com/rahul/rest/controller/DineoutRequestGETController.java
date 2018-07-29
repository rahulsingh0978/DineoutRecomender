package com.rahul.rest.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.rahul.beans.DineoutInput;


@Controller
public class DineoutRequestGETController {

	private static final String outputString = "input is: %s value of x is: %s";

	@GetMapping("/dineoutRecommender/assignment")
	@ResponseBody
	public DineoutInput dineoutRecommenderAssignment(
			@RequestParam(name = "matrix", required = false, defaultValue = "Java Fan") String matrixString) {
		int x = processInputMatrix(matrixString);

		return new DineoutInput(String.format(outputString, matrixString, x + ""));
	}

	private int processInputMatrix(String matrix) {
		// TODO redundent in post
		return 9999;
	}
}
