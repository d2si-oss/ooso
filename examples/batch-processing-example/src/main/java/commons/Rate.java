package commons;

public class Rate {
    private String id;
    private String rate;

    public Rate(String line) {
        String[] fields = line.split(",");
        this.id = fields[0];
        this.rate = fields[1];
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }
}
