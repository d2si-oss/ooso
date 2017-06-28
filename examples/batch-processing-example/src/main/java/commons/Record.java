package commons;

import java.util.StringTokenizer;

public class Record {
    private String vendorID = "";
    private String tpepPickupDatetime = "";
    private String tpepDropoffDatetime = "";
    private String passengerCount = "";
    private String tripDistance = "";
    private String pickupLongitude = "";
    private String pickupLatitude = "";
    private String ratecodeID = "";
    private String storeAndFwdFlag = "";
    private String dropoffLongitude = "";
    private String dropoffLatitude = "";
    private String paymentType = "";
    private String fareAmount = "";
    private String extra = "";
    private String mtaTax = "";
    private String tipAmount = "";
    private String tollsAmount = "";
    private String improvementSurcharge = "";
    private String totalAmount = "";

    public Record() {
    }

    public Record(String line) {
        StringTokenizer tokenizer = new StringTokenizer(line, ",");
        if (tokenizer.hasMoreTokens())
            vendorID = tokenizer.nextToken();
        if (tokenizer.hasMoreTokens())
            tpepPickupDatetime = tokenizer.nextToken();
        if (tokenizer.hasMoreTokens())
            tpepDropoffDatetime = tokenizer.nextToken();
        if (tokenizer.hasMoreTokens())
            passengerCount = tokenizer.nextToken();
        if (tokenizer.hasMoreTokens())
            tripDistance = tokenizer.nextToken();
        if (tokenizer.hasMoreTokens())
            pickupLongitude = tokenizer.nextToken();
        if (tokenizer.hasMoreTokens())
            pickupLatitude = tokenizer.nextToken();
        if (tokenizer.hasMoreTokens())
            ratecodeID = tokenizer.nextToken();
        if (tokenizer.hasMoreTokens())
            storeAndFwdFlag = tokenizer.nextToken();
        if (tokenizer.hasMoreTokens())
            dropoffLongitude = tokenizer.nextToken();
        if (tokenizer.hasMoreTokens())
            dropoffLatitude = tokenizer.nextToken();
        if (tokenizer.hasMoreTokens())
            paymentType = tokenizer.nextToken();
        if (tokenizer.hasMoreTokens())
            fareAmount = tokenizer.nextToken();
        if (tokenizer.hasMoreTokens())
            extra = tokenizer.nextToken();
        if (tokenizer.hasMoreTokens())
            mtaTax = tokenizer.nextToken();
        if (tokenizer.hasMoreTokens())
            tipAmount = tokenizer.nextToken();
        if (tokenizer.hasMoreTokens())
            tollsAmount = tokenizer.nextToken();
        if (tokenizer.hasMoreTokens())
            improvementSurcharge = tokenizer.nextToken();
        if (tokenizer.hasMoreTokens())
            totalAmount = tokenizer.nextToken();
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

    public String getPickupLongitude() {
        return pickupLongitude;
    }

    public void setPickupLongitude(String pickupLongitude) {
        this.pickupLongitude = pickupLongitude;
    }

    public String getPickupLatitude() {
        return pickupLatitude;
    }

    public void setPickupLatitude(String pickupLatitude) {
        this.pickupLatitude = pickupLatitude;
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

    public String getDropoffLongitude() {
        return dropoffLongitude;
    }

    public void setDropoffLongitude(String dropoffLongitude) {
        this.dropoffLongitude = dropoffLongitude;
    }

    public String getDropoffLatitude() {
        return dropoffLatitude;
    }

    public void setDropoffLatitude(String dropoffLatitude) {
        this.dropoffLatitude = dropoffLatitude;
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

    @Override
    public String toString() {
        return String.join(",", vendorID, tpepPickupDatetime, tpepDropoffDatetime, passengerCount, tripDistance, pickupLongitude, pickupLatitude, ratecodeID, storeAndFwdFlag, dropoffLongitude, dropoffLatitude, paymentType, fareAmount, extra, mtaTax, tipAmount, tollsAmount, improvementSurcharge, totalAmount);
    }
}