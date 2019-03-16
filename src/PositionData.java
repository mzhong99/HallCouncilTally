public class PositionData {

    final int NUM_SEATS;
    final int NUM_CHOICES;

    public PositionData(int numSeats, int numChoices) {
        this.NUM_SEATS = numSeats;
        this.NUM_CHOICES = numChoices;
    }

    @Override
    public String toString() {
        return "{seats=" + NUM_SEATS + ", choices=" + NUM_CHOICES + "}";
    }
}
