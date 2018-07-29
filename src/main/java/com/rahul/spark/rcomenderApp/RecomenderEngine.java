package com.rahul.spark.rcomenderApp;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.mllib.recommendation.ALS;
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel;
import org.apache.spark.mllib.recommendation.Rating;

import com.amazonaws.services.simpleworkflow.flow.core.Promise;

import scala.Tuple2;

public class RecomenderEngine {

	public static String process(String[] args) {
		//args = new String[2];
//		args[0] = "D://MachineLearning/ml-latest-small/ratings_sample.csv";
//		args[1] = "D:\\MachineLearning\\ml-latest-small\\movies_sample1.csv";
		//args[1] = "10";
		//args[4] ="[[5,x,3,x,x,5],[x,3,x,0,4.5,2],[x,x,2,x,5,x],[4,x,x,x,x,x],[3,x,3,x,x,x]]";
		//args[0]="[[5,x,3,x,x,5],[x,3,x,0,4.5,2],[x,x,2,x,5,x],[4,x,x,x,x,x],[3,x,3,x,x,x]]";
	    String[] rows1 = args[0].split("\\],\\["); 
	    //to remove [[ and ]] from start and end
	    rows1[rows1.length-1] = rows1[rows1.length-1].substring(0, rows1[0].length()-2);
	    rows1[0] = rows1[0].substring(2, rows1[0].length());
	    String[][] matrix1 = new String[rows1.length][];
	    int r1 = 0;
	    for (String row : rows1) {
	        matrix1[r1++] = row.split(",");
	    }
	    List<String> inputRatingValues = new ArrayList<>();
	    List<Integer> incompleteRating = new ArrayList<>(); 
	    
	    for(int i = 0;i< rows1.length ;i++) {
	    	for(int j=0;j<=r1;j++) {
	    		//System.out.print((i+1)+":"+(j+1)+":"+matrix1[i][j]+" ");
	    		if(!matrix1[i][j].equalsIgnoreCase("x")) {
	    			inputRatingValues.add((i+1)+","+(j+1)+","+matrix1[i][j]);
	    		}
	    		else {
	    			incompleteRating.add(i+1);
	    		}
	    	}
	    	//System.out.println();
	    }
	    for( String values : inputRatingValues) {
	    	System.out.println(values);
	    }
		
		//String[][] matix = new String[m][n];

		Logger.getLogger("org").setLevel(Level.ERROR);

		// Create Java spark context
		SparkConf conf = new SparkConf().setAppName("DineOut Recomendation App").setMaster("local[2]");
		conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
		conf.set("spark.kryoserializer.buffer.max", "2000m");
		conf.set("spark.driver.allowMultipleContexts", "true");
		Class<?>[] classes = { RecomenderEngine.class };
		conf.registerKryoClasses(classes);
		JavaSparkContext sc = new JavaSparkContext(conf);

		// Read user-item rating file. format - userId,itemId,rating
		//JavaRDD<String> userItemRatingsFile = sc.textFile(args[0]);
		JavaRDD<String> userItemRatingsFile = sc.parallelize(inputRatingValues);

		// Read item description file. format - itemId, itemName, Other Fields,..
		//JavaRDD<String> itemDescritpionFile = sc.textFile(args[1]);

		// Map file to Ratings(user,item,rating) tuples
		JavaRDD<Rating> ratings = userItemRatingsFile.map(new Function<String, Rating>() {
			public Rating call(String s) {
				String[] sarray = s.split(",");
				return new Rating(Integer.parseInt(sarray[0]), Integer.parseInt(sarray[1]),
						Double.parseDouble(sarray[2]));
			}
		});
		// JavaRDD<String> ratings = userItemRatingsFile.map(new Function<String,
		// String>() {
		// public String call(String s) {
		// String[] sarray = s.split(",");
		// return sarray[0]+","+sarray[1]+","+ sarray[2];
		// }
		// });
		// Create tuples(itemId,ItemDescription), will be used later to get names of
		// item from itemId
//		JavaPairRDD<Integer, String> itemDescritpion = itemDescritpionFile
//				.mapToPair(new PairFunction<String, Integer, String>() {
//					@Override
//					public Tuple2<Integer, String> call(String t) throws Exception {
//						String[] s = t.split(",");
//						return new Tuple2<Integer, String>(Integer.parseInt(s[0]), s[1]);
//					}
//				});
		// Build the recommendation model using ALS

		int rank = 10; // 10 latent factors
		int numIterations = Integer.parseInt(args[1]); // number of iterations

		MatrixFactorizationModel model = ALS.train(JavaRDD.toRDD(ratings), rank, numIterations);
		// ALS.trainImplicit(arg0, arg1, arg2)

		// Create user-item tuples from ratings
		JavaRDD<Tuple2<Object, Object>> userProducts = ratings.map(new Function<Rating, Tuple2<Object, Object>>() {
			public Tuple2<Object, Object> call(Rating r) {
				return new Tuple2<Object, Object>(r.user(), r.product());
			}
		});

		// Calculate the itemIds not rated by a particular user, say user with userId
		// = 1
		StringBuffer finalOutput = new StringBuffer();
		for(int xRated :incompleteRating) {
		JavaRDD<Integer> notRatedByUser = userProducts.filter(new Function<Tuple2<Object, Object>, Boolean>() {

			@Override
			public Boolean call(Tuple2<Object, Object> v1) throws Exception {
				if (((Integer) v1._1).intValue() != xRated) {
					return true;
				}
				return false;
			}
		}).map(new Function<Tuple2<Object, Object>, Integer>() {

			@Override
			public Integer call(Tuple2<Object, Object> v1) throws Exception {
				return (Integer) v1._2;
			}
		});

		// Create user-item tuples for the items that are not rated by user, with
		// user id 1
		JavaRDD<Tuple2<Object, Object>> itemsNotRatedByUser = notRatedByUser
				.map(new Function<Integer, Tuple2<Object, Object>>() {
					public Tuple2<Object, Object> call(Integer r) {
						return new Tuple2<Object, Object>(xRated, r);
					}
				});

		// Predict the ratings of the items not rated by user for the user
		JavaRDD<Rating> recomondations = model.predict(itemsNotRatedByUser.rdd()).toJavaRDD().distinct();

		// JavaRDD<Rating> recomondations =
		// model.predict(itemsNotRatedByUser.rdd()).toJavaRDD().distinct();

		// Sort the recommendations by rating in descending order
		recomondations = recomondations.sortBy(new Function<Rating, Double>() {
			@Override
			public Double call(Rating v1) throws Exception {
				return v1.rating();
			}

		}, false, 1);

		// Get top 10 recommendations
		JavaRDD<Rating> topRecomondations = sc.parallelize(recomondations.take(r1+1));

		// Join top 10 recommendations with item descriptions
		System.out.println("recomendation");
		/*JavaRDD<Tuple2<Rating, String>> recommendedItems = topRecomondations
				.mapToPair(new PairFunction<Rating, Integer, Rating>() {

					@Override
					public Tuple2<Integer, Rating> call(Rating t) throws Exception {
						return new Tuple2<Integer, Rating>(t.product(), t);
					}
				}).join(itemDescritpion).values();
		 */	
		// recommendedItems.collect();

		System.out.println("toprecomendation");
		topRecomondations.foreach(new VoidFunction<Rating>() {
			@Override
			public void call(Rating rating) throws Exception {
				String str = "Userid : " + rating.user() + " Product : " + rating.product() + " Rating : "
						+ rating.rating();
				System.out.println(str);
			}
		});

		// recommendedItems.saveAsTextFile("D:\\MachineLearning\\ml-latest-small\\output2");
		// Print the top recommendations for user 1.
		System.out.println("recomendation");
		/*recommendedItems.foreach(new VoidFunction<Tuple2<Rating, String>>() {

			@Override
			public void call(Tuple2<Rating, String> t) throws Exception {
				System.out.println(t._1.product() + "\t" + t._1.rating() + "\t" + t._2);
			}
		});*/

		
		List<Rating> completeRecomendationList = topRecomondations.collect();
		for(Rating t :completeRecomendationList) {
			finalOutput.append(t.user() + "\t:" + t.product() + "\t:" + t.rating()+"|");
		}
		// recommendedItems.collect();

		//System.out.println("toprecomendation");
//		topRecomondations.foreach(new VoidFunction<Rating>() {
//			@Override
//			public void call(Rating rating) throws Exception {
//				String str = "User : " + rating.user() + " Product : " + rating.product() + " Rating : "
//						+ rating.rating();
//				System.out.println(str);
//			}
//		});

		// recommendedItems.saveAsTextFile("D:\\MachineLearning\\ml-latest-small\\output2");
		// Print the top recommendations for user 1.
//		System.out.println("recomendation");
//		recommendedItems.foreach(new VoidFunction<Tuple2<Rating, String>>() {
//
//			@Override
//			public void call(Tuple2<Rating, String> t) throws Exception {
//				//finalOutput.append(t._1.product() + "\t" + t._1.rating() + "\t" + t._2);
//				System.out.println(t._1.product() + "\t" + t._1.rating() + "\t" + t._2);
//			}
//		});
		}
		return finalOutput.toString();
	}

}

