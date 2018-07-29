package com.rahul.rest.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.rahul.beans.DineoutRecommender;
import com.rahul.beans.DineoutRecommenderReply;
import com.rahul.spark.rcomenderApp.RecomenderEngine;

@Controller
public class DineoutRecommenderPOSTController {

	@RequestMapping(method = RequestMethod.POST, value="/dineoutRecommender/findX")
	@ResponseBody
	DineoutRecommenderReply registerStudent(@RequestBody DineoutRecommender dineoutRecommenderInput) {
		DineoutRecommenderReply output = new DineoutRecommenderReply();
		
		String outputString=processDineoutRecommenderInputMatrix(dineoutRecommenderInput.getInput());

		
	///output.setM(dineoutRecommenderInput.getM());
	output.setInputMatrixString(dineoutRecommenderInput.getInput());
	//output.setN(dineoutRecommenderInput.getN());
	output.setOutputString(outputString);

	return output;
	}

	private String processDineoutRecommenderInputMatrix(String matrixInStringFormat) {
		// TODO 
		String []args = new String[2];
		args[0] = matrixInStringFormat;
		//args[1] = "D:\\MachineLearning\\ml-latest-small\\movies_sample.csv";
		args[1] = "10";
		
		
		return RecomenderEngine.process(args);
	}
	
}
