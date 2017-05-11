package core;

import Parser.TextReader;
import Parser.TextWriter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by sajad on 11/25/2016.
 */
public class VotesharePrime {

    private static HashMap<String,String> adjusted_voteshares;
    private static HashMap<String,String> voteshares;
    private static HashMap<String,String> answers_timespan;
    private int K = 1;
    private IndexReader iReader;
    private IndexSearcher iSearcher;


    private void init() throws IOException {
        adjusted_voteshares = new HashMap<>();
        answers_timespan = new HashMap<>();
        voteshares = new HashMap<>();
        iReader =  DirectoryReader.open(FSDirectory.open(Paths.get("D:\\University\\Information Retrieval 2\\Hws\\Hw1\\indexFiles\\Hw2Answers")));
        iSearcher = new IndexSearcher(iReader);
    }

    private HashMap<String, String> getVotesharesPrimeList() throws IOException, ParseException {
        init();
        HashMap<String,String> answers_timespan = TextReader.parseTimeSpans("in-files/AnswersTimespan.csv");
        voteshares = TextReader.read_process("in-files\\voteShare.txt", "\t");
        getAdjustedVoteshares(answers_timespan);
        HashMap<String, String> answers_votesharePrime = new HashMap<>();
        adjusted_voteshares.remove("-1");
        voteshares.remove("-1");
        for (String answer_Id : voteshares.keySet()){
            String q_id = getQuestionId(answer_Id);
            ArrayList<String> thread_answers_id;
            thread_answers_id = getThreadAnswersId(q_id);
            answers_votesharePrime.put(answer_Id , getVotesharePrime(answer_Id, thread_answers_id) );
        }
        return answers_votesharePrime;
    }

    private String getQuestionId(String a_id) throws ParseException, IOException {
        QueryParser parser = new QueryParser("docId", new StandardAnalyzer());
        Query query = parser.parse(a_id);
        TopDocs hits = iSearcher.search(query, 10);
        Document doc = new Document();
        int i;
        for (i=0; i<hits.scoreDocs.length; i++){
            doc = iSearcher.doc(hits.scoreDocs[i].doc);
        }
        if (doc.get("questionId") == null){
            System.out.println("salan");
        }
        return doc.get("questionId");
    }

    private ArrayList<String> getThreadAnswersId(String q_id) throws ParseException, IOException {
        QueryParser parser = new QueryParser("questionId", new StandardAnalyzer());
//        if (q_id == null)
//        {
//            System.out.println("saadsak");
//        }
        Query query = parser.parse(q_id);
        TopDocs hits = iSearcher.search(query, 100);
        ArrayList<String> thread_answers_id = new ArrayList<>();
        for (int i=0; i<hits.scoreDocs.length; i++){
            Document doc = iSearcher.doc(hits.scoreDocs[i].doc);
            thread_answers_id.add(doc.getField("docId").stringValue());
        }
        return thread_answers_id;
    }

    private String getVotesharePrime(String answer_Id, ArrayList<String> thread_answers_id) {
        double sum_of_adjusted_voteshares = 0;
        if (checkExistence(answer_Id, thread_answers_id)) {

            for (String id : thread_answers_id) {
                    sum_of_adjusted_voteshares += Double.parseDouble(adjusted_voteshares.get(id));
            }

            if (sum_of_adjusted_voteshares == 0 )
                return "0";

            return String.valueOf(Double.parseDouble(adjusted_voteshares.get(answer_Id)) / sum_of_adjusted_voteshares);
        }
        else
        {
            sum_of_adjusted_voteshares += Double.parseDouble(voteshares.get(answer_Id));
            for (String id : thread_answers_id) {
                    sum_of_adjusted_voteshares += Double.parseDouble(voteshares.get(id));
            }
            return String.valueOf(Double.parseDouble(voteshares.get(answer_Id)) / sum_of_adjusted_voteshares);
        }
    }

    private boolean checkExistence(String answer_id, ArrayList<String> thread_answers_id) {
        boolean check = adjusted_voteshares.containsKey(answer_id);
        for (String id : thread_answers_id){
            check = check && adjusted_voteshares.containsKey(id);
        }
        return check;
    }

    private void getAdjustedVoteshares(HashMap<String, String> answers_timespan) {
        for (String docId : voteshares.keySet()) {
            if (Objects.equals(docId, "AId")) continue;
            try {
                adjusted_voteshares.put(docId, String.valueOf(Double.parseDouble(voteshares.get(docId)) / getAdjustedDenominator(answers_timespan.get(docId))));
            }
            catch (NumberFormatException ne){
                ne.printStackTrace();
            }
        }
    }

    private double getAdjustedDenominator(String timespan) {
        if (timespan==null){
            return 1.0;
        }
        return (1.0 - Math.pow(Math.E, -K * (Double.parseDouble(timespan))));
    }

    public static void main(String[] args) throws IOException, ParseException {
        VotesharePrime v = new VotesharePrime();
        HashMap<String , String > votesharesPrime = v.getVotesharesPrimeList();
        TextWriter.writeToCsv(votesharesPrime, "voteSharePrime");
    }
}
