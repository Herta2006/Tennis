package samples.tennis;

import javafx.util.Pair;

public class AtpTennisPlayer implements Comparable<AtpTennisPlayer> {
    private String atpUrl;
    private int rank;
    private String firstName;
    private String surname;
    private ServiceRecord serviceRecord;
    private AbstractRecord returnRecord;

    public AtpTennisPlayer(String atpUrl) {
        this.atpUrl = atpUrl;
    }

    public String getAtpUrl() {
        return atpUrl;
    }

    public void setAtpUrl(String atpUrl) {
        this.atpUrl = atpUrl;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public ServiceRecord getServiceRecord() {
        return serviceRecord;
    }

    public void setServiceRecord(ServiceRecord serviceRecord) {
        this.serviceRecord = serviceRecord;
    }

    public AbstractRecord getReturnRecord() {
        return returnRecord;
    }

    public void setReturnRecord(AbstractRecord returnRecord) {
        this.returnRecord = returnRecord;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AtpTennisPlayer that = (AtpTennisPlayer) o;

        return atpUrl.equals(that.atpUrl);

    }

    @Override
    public int hashCode() {
        return atpUrl.hashCode();
    }

    @Override

    public int compareTo(AtpTennisPlayer atpTennisPlayer) {
        return this.rank == atpTennisPlayer.rank ? 0 : this.rank > atpTennisPlayer.rank ? 1 : -1;
    }

    @Override
    public String toString() {
        return "TennisPlayer{" +
                "rank=" + rank +
                ", firstName='" + firstName + '\'' +
                ", surname='" + surname + '\'' +
                '}';
    }

    static class ServiceRecord extends AbstractRecord {
        private Pair<String, Integer> aces;
        private Pair<String, Integer> doubleFaults;

        public Pair<String, Integer> getAces() {
            return aces;
        }

        public void setAces(Pair<String, Integer> aces) {
            this.aces = aces;
        }

        public Pair<String, Integer> getDoubleFaults() {
            return doubleFaults;
        }

        public void setDoubleFaults(Pair<String, Integer> doubleFaults) {
            this.doubleFaults = doubleFaults;
        }
    }

    static class AbstractRecord {
        private Pair<String, Integer> pointsWon;
        private Pair<String, Integer> firstServePointsWon;
        private Pair<String, Integer> secondServePointsWon;
        private Pair<String, Integer> breakPointsFaced;
        private Pair<String, Integer> breakPointsWon;
        private Pair<String, Integer> totalPointsWon;
        private Pair<String, Integer> gamesPlayed;
        private Pair<String, Integer> gamesWon;

        public Pair<String, Integer> getPointsWon() {
            return pointsWon;
        }

        public void setPointsWon(Pair<String, Integer> pointsWon) {
            this.pointsWon = pointsWon;
        }

        public Pair<String, Integer> getFirstServePointsWon() {
            return firstServePointsWon;
        }

        public void setFirstServePointsWon(Pair<String, Integer> firstServePointsWon) {
            this.firstServePointsWon = firstServePointsWon;
        }

        public Pair<String, Integer> getSecondServePointsWon() {
            return secondServePointsWon;
        }

        public void setSecondServePointsWon(Pair<String, Integer> secondServePointsWon) {
            this.secondServePointsWon = secondServePointsWon;
        }

        public Pair<String, Integer> getBreakPointsFaced() {
            return breakPointsFaced;
        }

        public void setBreakPointsFaced(Pair<String, Integer> breakPointsFaced) {
            this.breakPointsFaced = breakPointsFaced;
        }

        public Pair<String, Integer> getBreakPointsWon() {
            return breakPointsWon;
        }

        public void setBreakPointsWon(Pair<String, Integer> breakPointsWon) {
            this.breakPointsWon = breakPointsWon;
        }

        public Pair<String, Integer> getTotalPointsWon() {
            return totalPointsWon;
        }

        public void setTotalPointsWon(Pair<String, Integer> totalPointsWon) {
            this.totalPointsWon = totalPointsWon;
        }

        public Pair<String, Integer> getGamesPlayed() {
            return gamesPlayed;
        }

        public void setGamesPlayed(Pair<String, Integer> gamesPlayed) {
            this.gamesPlayed = gamesPlayed;
        }

        public Pair<String, Integer> getGamesWon() {
            return gamesWon;
        }

        public void setGamesWon(Pair<String, Integer> gamesWon) {
            this.gamesWon = gamesWon;
        }
    }
}
