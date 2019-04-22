package com.lg;

import org.apache.commons.lang3.StringUtils;

import info.debatty.java.stringsimilarity.Jaccard;
import info.debatty.java.stringsimilarity.JaroWinkler;
import info.debatty.java.stringsimilarity.Levenshtein;

public class countDuplication {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String a = "Login the Linux machine > run \"csetaccount <account name>\" to add an account";
		String b = "Login the Linux machine";
		JaroWinkler levenshtein = new JaroWinkler();
		Jaccard ja = new Jaccard();
		System.out.println(levenshtein.similarity(a, b));
		System.out.println(ja.similarity(a, b));
	}

}
