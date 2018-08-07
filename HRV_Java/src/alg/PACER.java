package alg;

public class PACER{
    public int getMmSecUsed() {
        return mmSecUsed;
    }

    public void setMmSecUsed(int mmSecUsed) {
        this.mmSecUsed = mmSecUsed;
    }

    private int mmSecUsed=0;

    public int getPercentage() {
        return percentage;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }

    private int percentage=0;

    public boolean isInHallsing() {
        return inHallsing;
    }

    public void setInHallsing(boolean inHallsing) {
        this.inHallsing = inHallsing;
    }

    private boolean inHallsing=true;

    public PACER(int mmsec,int percent, boolean inHallsing)
    {
        setMmSecUsed(mmsec);
        setPercentage(percent);
        setInHallsing(inHallsing);
    }

}
