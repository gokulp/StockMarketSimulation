import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by gokul on 10/24/15.
 */
public class Share /*implements Runnable*/ {
    public double[] netWorth = new double[100];
    //public double[] volatility = new double[100];
    public double volatility = 0.0;
    public double[] dividend = new double[100];
    public double[] bid = new double[100];
    public double[] ask = new double[100];
    public int bufferIndex;
    public String company;
    public String host;
    public String port;
    public String username;
    public String password;
    public int companyIndex;
    private double prevNetWorth = 0.0;
    public String trend;
    public double baseNetWorth = 0.0;
    public double diffNetWorth = 0.0;
    private int count = 0;
    private int contInc = 0;
    private int contDec = 0;
    public double maxNetWorth = 0.0;
    public double minNetWorth = 0.0;
    public int shareQuantity = 0;


    //public Share(String hostName, String port, String username, String password, String companyName, int index) {
    public Share(String companyName, int index) {
        this.company = companyName;
        this.companyIndex = index;
    }

/*
    @Override
    public void run() {
        try {
            trackTrend();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
*/

    public synchronized void trackTrend(PrintWriter pout, BufferedReader bin) throws IOException {
        //int i = 0;
        count = 0;

        //Socket socket = new Socket(host, Integer.parseInt(port));
        //PrintWriter pout = new PrintWriter(socket.getOutputStream());
        //BufferedReader bin = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        //pout.println(username + " " + password);
        //while(i < 20){
        //System.out.println("SECURITIES\nORDERS "+company);
        pout.println("SECURITIES\nORDERS " + company);
        pout.flush();
        //System.out.println(bin.readLine());
        String[] securityLine = bin.readLine().toString().split(" ");
        String[] bidLine = bin.readLine().toString().split(" ");
        int index = 1;
        if (securityLine.length <= 0)
            return;

        //Put Maximum bid value in array
        while(!bidLine[index].equals("BID")) {
            index += 4;
        }
        bid[bufferIndex] = Double.parseDouble(bidLine[index + 2]);

        //Put Minimum Ask value in array
        while(!bidLine[index].equals("ASK")){
            index += 4;
        }
        ask[bufferIndex] = Double.parseDouble(bidLine[index + 2]);

        netWorth[bufferIndex] = Double.parseDouble(securityLine[companyIndex * 4 + 2]);
        dividend[bufferIndex] = Double.parseDouble(securityLine[companyIndex * 4 + 3]);
        volatility = Double.parseDouble(securityLine[companyIndex * 4 + 4]);

        if (prevNetWorth == 0.0 ){
            baseNetWorth = netWorth[bufferIndex];
        }

        if( netWorth[bufferIndex] < prevNetWorth) {
            count--;
            contDec++;
            contInc = 0;
            //can store variance as well
        } else if (netWorth[bufferIndex] > prevNetWorth) {
            count++;
            contDec = 0;
            contInc++;
            //can store variance as well
        }
        if (netWorth[bufferIndex] < minNetWorth )
            minNetWorth = netWorth[bufferIndex];
        if (netWorth[bufferIndex] > maxNetWorth)
            maxNetWorth = netWorth[bufferIndex];
        prevNetWorth = netWorth[bufferIndex];
        diffNetWorth = netWorth[bufferIndex] - baseNetWorth;
        //++i;
        bufferIndex = (bufferIndex + 1)%100;
        //}
        //pout.println("CLOSE_CONNECTION");
        pout.flush();
        if (count > 20){
            setIncreasing();
        } else if (count < -20){
            setDecreasing();
        } else {
            setOscillating();
        }
    }

    public double getBidValue(){
        if (bufferIndex == 0)
            bufferIndex = 100;
        double retValue = bid[bufferIndex -1];
        bufferIndex = bufferIndex == 100 ? 0 :bufferIndex;
        return(retValue);
    }

    public double getAskValue(){
        if (bufferIndex == 0)
            bufferIndex = 100;
        double retValue = ask[bufferIndex - 1];
        bufferIndex = bufferIndex == 100 ? 0 :bufferIndex;
        return(retValue);
    }

    private void setOscillating() {
        //System.out.println(company+"is oscillating");
        trend = "OS";
    }

    private void setDecreasing() {
        //System.out.println(company+"is Decreasing");
        trend = "CD";
    }

    private void setIncreasing() {
        //System.out.println(company+"is Increasing");
        trend = "CI";
    }

    public boolean isIncreasing(){
        if (trend.equals("CI"))
            return(true);
        return (false);
    }

    public boolean isDecreasing(){
        if (trend.equals("CD")){
            return(true);
        }
        return (false);
    }

    public boolean isOscillating(){
        if (trend.equals("OS"))
            return(true);

        return(false);
    }

    public boolean sellStock(double purchasePrice){
        if (bufferIndex == 0)
            bufferIndex = 100;

        if (isBestPerforming())
            return(false);

        if (isDecreasing()) {
/*
            if ((1 - volatility) * baseNetWorth * 0.99 < netWorth[bufferIndex - 1]) {
                bufferIndex = bufferIndex == 100 ? 0 : bufferIndex;
                return (false);
            }
*/
            bufferIndex = bufferIndex == 100 ? 0 : bufferIndex;
            return (true);
        } else if (isOscillating()){
            if (ask[bufferIndex - 1] >= purchasePrice &&
                    netWorth[bufferIndex - 1] > baseNetWorth &&
                    (1 + volatility)*baseNetWorth*0.95 < netWorth[bufferIndex -1]){
                bufferIndex = bufferIndex == 100? 0:bufferIndex;
                return (true);
            } else if (netWorth[bufferIndex - 1] < baseNetWorth) {
                bufferIndex = bufferIndex == 100? 0:bufferIndex;
                return (true);
            }
        } else if (isIncreasing()){
            if (dividend[bufferIndex -1] < 0.001 && (1 + volatility)*baseNetWorth*0.95 < netWorth[bufferIndex -1]) {
                bufferIndex = bufferIndex == 100? 0:bufferIndex;
                return(true);
            }
        }
        bufferIndex = bufferIndex == 100? 0:bufferIndex;
        return(false);
    }

    public boolean buyStock(double bidPrice){
        if (bufferIndex == 0)
            bufferIndex = 100;

        if(isWorstPerforming())
            return (false);

        if (baseNetWorth * 2 > netWorth[bufferIndex -1])
            return(true);

        if (bidPrice < bid[bufferIndex - 1] && bidPrice < ask[bufferIndex -1]) {
            bufferIndex = bufferIndex == 100? 0:bufferIndex;
            return (false);
        }

        if (isDecreasing()){
            bufferIndex = bufferIndex == 100? 0:bufferIndex;
            return(false);
/*
            if ((1 - volatility)*baseNetWorth*0.85 > netWorth[bufferIndex -1]) {
                bufferIndex = bufferIndex == 100? 0:bufferIndex;
                return (true);
            }
*/
        } if (isIncreasing()){
            bufferIndex = bufferIndex == 100? 0:bufferIndex;
            return(true);
/*
            if ((1 - volatility)*baseNetWorth*0.5 < netWorth[bufferIndex -1]) {
                bufferIndex = bufferIndex == 100? 0:bufferIndex;
                return (true);
            }
*/
        } if (isOscillating()){
            if (dividend[bufferIndex -1] > 0.1) {
                bufferIndex = bufferIndex == 100? 0:bufferIndex;
                return (true);
            }
            if((1 + volatility)*baseNetWorth*0.5 < netWorth[bufferIndex -1]) {
                bufferIndex = bufferIndex == 100? 0:bufferIndex;
                return (true);
            }
        }
        bufferIndex = bufferIndex == 100? 0:bufferIndex;
        return(false);
    }

    public void getShareInfo(PrintWriter pout, BufferedReader bin) throws IOException {
        pout.println("MY_SECURITIES");
        pout.flush();
        String[] strarr = bin.readLine().toString().split(" ");
        shareQuantity = Integer.parseInt(strarr[companyIndex*3 + 2]);
    }

    public boolean isWorstPerforming(){
        if (bufferIndex == 0)
            bufferIndex = 100;

        if (baseNetWorth * 0.90 > netWorth[bufferIndex - 1]){
            bufferIndex = bufferIndex == 100? 0:bufferIndex;
            return (true);
        }

        return (false);
    }

    public double checkAndClearExistingOrder(PrintWriter pout, BufferedReader bin) throws IOException {
        pout.println("MY_ORDERS");
        pout.flush();
        double retValue = -1.0; //Indicates no value was present
        String [] strarr = bin.readLine().toString().split(" ");
        for (int i = 2; i < strarr.length; i += 4) {
            if (strarr[i].equals(company)){
                retValue = Double.parseDouble(strarr[i+1]);
                pout.println("CLEAR_ASK "+company);
                pout.flush();
                System.out.println(bin.readLine()); // Careful while commenting
                break;
            }
        }
        return(retValue);
    }

    public boolean isBestPerforming() {
        if (bufferIndex == 0)
            bufferIndex = 100;
        if (baseNetWorth * 1.5 < netWorth[bufferIndex - 1]) {
            bufferIndex = bufferIndex == 100? 0:bufferIndex;
            return (true);
        }

        return false;
    }

    public void sellEverything(PrintWriter pout, BufferedReader bin) throws IOException {
        double bidValue = getBidValue();
        pout.println("ASK "+company+" "+bidValue+" "+shareQuantity);
        pout.flush();
        System.out.println(bin.readLine());
    }
}
