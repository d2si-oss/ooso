package commons;

public class Record {
    private String vendorID;
    private String tpepPickupDatetime;
    private String tpepDropoffDatetime;
    private String passengerCount;
    private String tripDistance;
    private String ratecodeID;
    private String storeAndFwdFlag;
    private String PULocationID;
    private String DOLocationID;
    private String paymentType;
    private String fareAmount;
    private String extra;
    private String mtaTax;
    private String tipAmount;
    private String tollsAmount;
    private String improvementSurcharge;
    private String totalAmount;

    public Record() {
    }

    public Record(String line) {
        String[] data = line.split(",");
        vendorID = data[0];
        tpepPickupDatetime = data[1];
        tpepDropoffDatetime = data[2];
        passengerCount = data[3];
        tripDistance = data[4];
        ratecodeID = data[5];
        storeAndFwdFlag = data[6];
        PULocationID = data[7];
        DOLocationID = data[8];
        paymentType = data[9];
        fareAmount = data[10];
        extra = data[11];
        mtaTax = data[12];
        tipAmount = data[13];
        tollsAmount = data[14];
        improvementSurcharge = data[15];
        totalAmount = data[16];
    }

    public String getVendorID() {
        return vendorID;
    }

    public void setVendorID(String vendorID) {
        this.vendorID = vendorID;
    }

    public String getTpepPickupDatetime() {
        return tpepPickupDatetime;
    }

    public void setTpepPickupDatetime(String tpepPickupDatetime) {
        this.tpepPickupDatetime = tpepPickupDatetime;
    }

    public String getTpepDropoffDatetime() {
        return tpepDropoffDatetime;
    }

    public void setTpepDropoffDatetime(String tpepDropoffDatetime) {
        this.tpepDropoffDatetime = tpepDropoffDatetime;
    }

    public String getPassengerCount() {
        return passengerCount;
    }

    public void setPassengerCount(String passengerCount) {
        this.passengerCount = passengerCount;
    }

    public String getTripDistance() {
        return tripDistance;
    }

    public void setTripDistance(String tripDistance) {
        this.tripDistance = tripDistance;
    }

    public String getRatecodeID() {
        return ratecodeID;
    }

    public void setRatecodeID(String ratecodeID) {
        this.ratecodeID = ratecodeID;
    }

    public String getStoreAndFwdFlag() {
        return storeAndFwdFlag;
    }

    public void setStoreAndFwdFlag(String storeAndFwdFlag) {
        this.storeAndFwdFlag = storeAndFwdFlag;
    }

    public String getPULocationID() {
        return PULocationID;
    }

    public void setPULocationID(String PULocationID) {
        this.PULocationID = PULocationID;
    }

    public String getDOLocationID() {
        return DOLocationID;
    }

    public void setDOLocationID(String DOLocationID) {
        this.DOLocationID = DOLocationID;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getFareAmount() {
        return fareAmount;
    }

    public void setFareAmount(String fareAmount) {
        this.fareAmount = fareAmount;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public String getMtaTax() {
        return mtaTax;
    }

    public void setMtaTax(String mtaTax) {
        this.mtaTax = mtaTax;
    }

    public String getTipAmount() {
        return tipAmount;
    }

    public void setTipAmount(String tipAmount) {
        this.tipAmount = tipAmount;
    }

    public String getTollsAmount() {
        return tollsAmount;
    }

    public void setTollsAmount(String tollsAmount) {
        this.tollsAmount = tollsAmount;
    }

    public String getImprovementSurcharge() {
        return improvementSurcharge;
    }

    public void setImprovementSurcharge(String improvementSurcharge) {
        this.improvementSurcharge = improvementSurcharge;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }
}