package top.srcandy.candyterminal.enums;

public enum SMSChannel {
    REGISTER("1021"),LOGIN("1008");

    private final String serviceCode;

    SMSChannel(String serviceCode){
        this.serviceCode = serviceCode;
    }

    public String getServiceCode(){
        return serviceCode;
    }
}
