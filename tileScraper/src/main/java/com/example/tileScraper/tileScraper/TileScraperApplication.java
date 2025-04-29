package com.example.tileScraper.tileScraper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TileScraperApplication {

	public static void main(String[] args) {

//		for(String arg : args) {
//			System.out.println(arg);
//		}
//	if(args.length > 0){
//		System.out.println("First argument in the list of arguments is: " + args[0]);
//	}
		SpringApplication.run(TileScraperApplication.class, args);
	}

}
