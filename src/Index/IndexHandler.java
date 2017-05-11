package VoteshareBasedRetrieval.Index; /**
 * Created by sajad on 10/19/2016.
 */
import Index.Answer;
import Index.Indexer;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.util.HashMap;

public class IndexHandler extends DefaultHandler {

    private HashMap<String, String> postIDs;
    private HashMap<String, String> vote_shares;
    private HashMap<String, String> vote_shares_timespan;
    private HashMap<String, String> answer_tags;

    private Indexer indxr;
    private Answer answer;
    private String nIndxDirectory;


    public IndexHandler(HashMap<String, String> hs, HashMap<String, String> vShare, HashMap<String, String> answer_Tags, HashMap<String, String> vShare_timespan) throws IOException {
        postIDs = new HashMap<>();
        vote_shares = new HashMap<>();
        vote_shares_timespan = new HashMap<>();
        answer_tags = new HashMap<>();
        postIDs = hs;
        vote_shares = vShare;
        vote_shares_timespan = vShare_timespan;
        answer_tags = answer_Tags;
        nIndxDirectory = "C:\\paper_projects\\files";
        indxr = new Indexer("C:\\paper_projects\\indexes");
    }


    @Override
    public void startElement(String uri,
                             String localName, String qName, Attributes attributes)
            throws SAXException {
        if (qName.equalsIgnoreCase("row")) {
            if ( attributes.getValue("PostTypeId").equals("2") && postIDs.containsKey(attributes.getValue("Id")) ){
                String body = attributes.getValue("Body");
                String docId = attributes.getValue("Id");
                String oUserId = attributes.getValue("OwnerUserId");
                String score = attributes.getValue("Score");
                String q_id = attributes.getValue("ParentId");
                try {
                    if (body != null  && oUserId != null) {
                        answer = new Answer();
                        answer.setBody(body);
                        answer.setDocId(docId);
                        answer.setOwnerUserId(oUserId);
                        answer.setScore(score);
                        answer.setTagList(answer_tags.get(docId).split("#"));
                        answer.setQuestionId(q_id);
                        answer.setVoteShare(vote_shares.get(docId));
                        answer.setVoteShareWithTimespan(vote_shares_timespan.get(docId));
                        indxr.indexAnswers(answer);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}