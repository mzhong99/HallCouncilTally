import java.io.*;
import java.util.*;

public class VotingTally {

    private static final String COMMA_DELIMITER = ",";

    private List<List<String>> rawVotingData = new ArrayList<List<String>>();
    private Map<String, PositionData> metaData = new TreeMap<String, PositionData>();
    private Map<String, Integer> offsets = new HashMap<String, Integer>();

    private int numVotes;

    private Map<String, List<String>> victors = new TreeMap<String, List<String>>();
    private Map<String, String> electionLogs = new TreeMap<String, String>();

    private boolean parseSuccessful = true;

    public VotingTally(File dataFile, File metadataFile) {
        parseMetaData(metadataFile);
        parseData(dataFile);

        determineVictors();
    }

    public ResultData getResultsFor(String position) {
        return new ResultData(victors.get(position), electionLogs.get(position));
    }

    public List<String> getPositions() {
        return new ArrayList<String>(metaData.keySet());
    }

    private void determineVictors() {

        // if your parse failed don't do anything
        if (!parseSuccessful) {
            return;
        }

        // determine who won for each position as specified in metadata
        metaData.keySet().forEach(position -> determineVictorOf(position));
    }

    private void determineVictorOf(String position) {

        // Step zero: determine how many votes you need to win the position
        int seatsForPosition = metaData.get(position).NUM_SEATS;
        int quotaToWin = Math.floorDiv(numVotes, seatsForPosition + 1) + 1;
        StringBuilder log = new StringBuilder();

        // Print some information about which position is being calculated
        log.append("Results for " + position + " (" + quotaToWin + " votes required):\n");

        // Make a copy of the section of the table for this position.
        // We eliminate the people who lose by removing their first vote
        List<List<String>> votes = new ArrayList<List<String>>();

        // Need to count up how many votes each candidate earned
        Map<String, Integer> activeCandidates = new TreeMap<String, Integer>();

        // For storing victors in order
        List<String> victors = new LinkedList<String>();

        // First pass: figure out who's running for this position
        // and build the subtable of votes just for this position
        for (int row = 0; row < numVotes; row++) {

            List<String> ballot = new LinkedList<String>();

            for (int col = offsets.get(position);
                 col < offsets.get(position) + metaData.get(position).NUM_CHOICES;
                 col++) {

                String candidate = rawVotingData.get(row).get(col);
                ballot.add(candidate);
                activeCandidates.putIfAbsent(candidate, 0);
            }

            votes.add(ballot);
        }

        // Second pass: start percolating votes.
        // You keep percolating votes until the number of candidates remaining is
        // equal to number of seats offered for that position
        int round = 1;
        while (victors.size() != seatsForPosition) {

            // GOAL: Determine who got the least number of votes for this iteration
            // GOAL: Determine if anyone got enough votes to win directly

            // First, count up how many votes each person got
            // Precondition: each ballot has at least one choice, and each choice is a candidate still running
            for (List<String> ballot : votes) {
                String nextChoice = ballot.get(0);
                activeCandidates.put(nextChoice, activeCandidates.get(nextChoice) + 1);
            }

            // Next, show information for who is currently still running
            // Includes each candidate's name and how many votes were distributed to them this round
            log.append("Round " + round + ":\n");
            for (Map.Entry<String, Integer> candidate : activeCandidates.entrySet()) {
                log.append("    " + candidate.getKey() + ": " + candidate.getValue() + " votes\n");
            }

            // Mark the candidates which won directly by surpassing the quota
            for (Map.Entry<String, Integer> candidate : activeCandidates.entrySet()) {
                if (candidate.getValue() >= quotaToWin) {
                    victors.add(candidate.getKey());
                    log.append(candidate.getKey() + " directly wins with " + candidate.getValue() + " votes\n");
                }
            }

            // Remove the ballots of people who chose a candidate who won directly this round
            // and from the ballots which are empty (chose no more relevant cases)
            votes.removeIf(ballot -> victors.contains(ballot.get(0)));

            // Remove victor from ballots still in play (if they chose the victor as 2nd, 3rd, etc choice)
            for (String victor : victors) {
                activeCandidates.remove(victor);
                for (List<String> ballot : votes) {
                    ballot.removeIf(choice -> choice.equals(victor));
                }
            }

            // Then, determine who had the least number of votes. This candidate is eliminated.
            // I treat each candidate as a Map.Entry where the key is the candidate name
            // and the value is the number of votes distributed to that candidate this round
            Map.Entry<String, Integer> worstCandidate = activeCandidates.entrySet().iterator().next();
            for (Map.Entry<String, Integer> candidate : activeCandidates.entrySet()) {
                if (candidate.getValue() < worstCandidate.getValue()) {
                    worstCandidate = candidate;
                }
            }

            // Log results and who was eliminated
            log.append("Candidate eliminated: " + worstCandidate.getKey() + "\n");

            // Eliminate worst choice from each ballot
            // For any accidental duplicates, remove all of them (use lambda for each element to remove)
            final String candidateToRemove = worstCandidate.getKey();
            votes.forEach(ballot -> ballot.removeIf(choice -> choice.equals(candidateToRemove)));
            activeCandidates.remove(candidateToRemove);

            // After a round, remove all ballots which have no more remaining candidates still in play
            votes.removeIf(ballot -> ballot.isEmpty());

            // Reset vote counts for next round
            activeCandidates.keySet().forEach(person -> activeCandidates.put(person, 0));

            // Increment round counter for easier readability
            round++;
        }

        // Store results in class-scope data structures for who won and how they won
        this.victors.put(position, victors);
        this.electionLogs.put(position, log.toString());
    }

    private void parseMetaData(File metaDataFile) {

        try (BufferedReader reader = new BufferedReader(new FileReader(metaDataFile))) {

            // First line should have a list of each unique position
            // Ex: Freshman Rep, Secretary
            String positionsLine = reader.readLine();
            String[] positions = positionsLine.split(COMMA_DELIMITER);

            List<String> keys = Arrays.asList(positions);

            // Second line should have number of choices a ballot has for each position
            String numChoicesLine = reader.readLine();
            String[] numChoicesStrings = numChoicesLine.split(COMMA_DELIMITER);

            List<Integer> choicesValues = new ArrayList<Integer>();
            for (String rawNumChoice : numChoicesStrings) {
                choicesValues.add(Integer.parseInt(rawNumChoice));
            }

            // Third line should have number of seats we want for that position
            // Realistically we only worry about this for Freshman Rep which has 2 seats instead of 1
            String seatsAvailableLine = reader.readLine();
            String[] seatsAvailableStrings = seatsAvailableLine.split(COMMA_DELIMITER);

            List<Integer> seatsValues = new ArrayList<Integer>();
            for (String rawSeatsAvailable : seatsAvailableStrings) {
                seatsValues.add(Integer.parseInt(rawSeatsAvailable));
            }

            // offset from table parsed in for easier parsing
            int currentOffset = 0;
            for (int i = 0; i < positions.length; i++) {
                PositionData positionData = new PositionData(seatsValues.get(i), choicesValues.get(i));
                metaData.put(keys.get(i), positionData);
                offsets.put(keys.get(i), currentOffset);
                currentOffset += choicesValues.get(i);
            }
        }
        catch (Exception exception) {
            parseSuccessful = false;
        }
    }

    private void parseData(File dataFile) {
        if (!parseSuccessful) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {

            // toss the first line, we already get that from the metadata file
            reader.readLine();

            for (String line = reader.readLine(); line != null; line = reader.readLine()) {

                // toss first column, useless timestamp
                String[] ballot = line.split(COMMA_DELIMITER);
                ballot = Arrays.copyOfRange(ballot, 1, ballot.length);

                rawVotingData.add(Arrays.asList(ballot));
            }

            numVotes = rawVotingData.size();
        }
        catch (Exception exception) {
            parseSuccessful = false;
        }
    }
}