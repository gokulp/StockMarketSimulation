
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Client {

    public static HashMap<String, Double> SellAskMap = new HashMap<String, Double>();
    public static HashMap<String, Double> BuyBidMap = new HashMap<String, Double>();
    public static HashMap<String, Double> BuyPriceHistoryMap = new HashMap<String, Double>();
    public static HashMap<String, Integer> BuyQntHistoryMap = new HashMap<String, Integer>();

    public static int buffer_sum = 200;
    public static int my_sum = 900;
    public static int cap = 150;
    public static String[] companies = {"AAPL","ATVI","EA","FB", "GOOG","MSFT","SBUX","SNY","TSLA","TWTR"};

    public static void main(String[] args) throws IOException, InterruptedException {
        final Share[] shares = new Share[10];
        int i = 0;
        for (String company : companies) {
            shares[i] = new Share(company, i);
            i++;
        }

        Runnable Initialize = new Runnable() {
            public void run() {
                Socket socket = null;
                PrintWriter pout = null;
                BufferedReader bin =null;
                try {
                    socket = new Socket("codebb.cloudapp.net", 17429);
                    pout = new PrintWriter(socket.getOutputStream());
                    bin = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    pout.println("bloomfilter" + " " + "abcd1234");
                    pout.flush();

                    String orderString = "";
                    for (int j = 0; j < 25; j++) {
                        shares[0].trackTrend(pout, bin);
                        shares[1].trackTrend(pout, bin);
                        shares[2].trackTrend(pout, bin);
                        shares[3].trackTrend(pout, bin);
                        shares[4].trackTrend(pout, bin);
                        shares[5].trackTrend(pout, bin);
                        shares[6].trackTrend(pout, bin);
                        shares[7].trackTrend(pout, bin);
                        shares[8].trackTrend(pout, bin);
                        shares[9].trackTrend(pout, bin);
                    }

                    int k = 0;
                    for (String company: companies){
                        Double maxBid = shares[k].getBidValue();
                        Double minAsk = shares[k].getAskValue();
                        pout.println("MY_CASH");
                        pout.flush();
                        Double totalCash = Double.parseDouble(bin.readLine().toString().split(" ")[1]);
                        int totalShares = (int) (totalCash*0.1/minAsk) ;
                        totalShares = totalShares > 10 ? 10 : totalShares;
                        if (totalShares == 0 && shares[k].isOscillating() ){
                            totalShares = 1;
                        }
                        //Should we buy stock
                        if(minAsk > 0 && shares[k].buyStock(minAsk) && totalShares > 0){
                            //System.out.println("BID "+company+" "+minAsk+" "+totalShares);
                            pout.println("BID "+company+" "+minAsk+" "+totalShares);
                            pout.flush();
                            System.out.println(bin.readLine());
                            BuyBidMap.put(companies[k], maxBid);
                            SellAskMap.put(companies[k],minAsk);
                            pout.flush();
                        }
                        k++;
                    }

                    pout.println("CLOSE_CONNECTION");
                    pout.flush();

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } finally {
                    pout.println("CLOSE_CONNECTION");
                    pout.flush();
                    pout.close();
                    try {
                        bin.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            }
        };

        Thread one = new Thread(Initialize);
        one.start();
        one.join();

        Runnable updateMaps = new Runnable() {
            public void run() {
                Socket socket = null;
                PrintWriter pout = null;
                BufferedReader bin =null;
                try {
                    socket = new Socket("codebb.cloudapp.net", 17429);
                    pout = new PrintWriter(socket.getOutputStream());
                    bin = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    pout.println("bloomfilter" + " " + "abcd1234");
                    pout.flush();
                    String orderString = "";
                    for (int j = 0; j < 20; j++) {
                        shares[0].trackTrend(pout, bin);
                        shares[1].trackTrend(pout, bin);
                        shares[2].trackTrend(pout, bin);
                        shares[3].trackTrend(pout, bin);
                        shares[4].trackTrend(pout, bin);
                        shares[5].trackTrend(pout, bin);
                        shares[6].trackTrend(pout, bin);
                        shares[7].trackTrend(pout, bin);
                        shares[8].trackTrend(pout, bin);
                        shares[9].trackTrend(pout, bin);
                    }

                    int k = 0;
                    for (String company: companies){
                        Double maxBid = shares[k].getBidValue();
                        Double minAsk = shares[k].getAskValue();
                        pout.println("MY_CASH");
                        pout.flush();
                        Double totalCash = Double.parseDouble(bin.readLine().toString().split(" ")[1]);
                        int totalShares = (int) (totalCash*0.1/minAsk);
                        //Should we buy stock
                        if(minAsk > 0 && shares[k].buyStock(minAsk) && totalShares > 0){
                            //System.out.println("BID "+company+" "+minAsk+" "+totalShares);
                            pout.println("BID "+company+" "+minAsk+" "+totalShares);
                            pout.flush();
                            System.out.println(bin.readLine());
                            BuyBidMap.put(companies[k], maxBid);
                            SellAskMap.put(companies[k],minAsk);
                            pout.flush();
                        }
                        k++;
                    }

                    pout.println("CLOSE_CONNECTION");
                    pout.flush();

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } finally {
                    pout.println("CLOSE_CONNECTION");
                    pout.flush();
                    pout.close();
                    try {
                        bin.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            }
        };

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(updateMaps, 0, 15, TimeUnit.MILLISECONDS);

        Socket socket = null;
        PrintWriter pout = null;
        BufferedReader bin =null;

        Runnable SellMaps = new Runnable() {
            public void run() {
                Socket socket = null;
                PrintWriter pout = null;
                BufferedReader bin =null;
                try {
                    socket = new Socket("codebb.cloudapp.net", 17429);
                    pout = new PrintWriter(socket.getOutputStream());
                    bin = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    pout.println("bloomfilter" + " " + "abcd1234");
                    pout.flush();
                    String line;

                    int i = 0;
                    for (String company: companies){
                        shares[i].getShareInfo(pout, bin);
                        //shares[i].trackTrend(pout, bin);
                        double maxBid = shares[i].getBidValue();
                        int numShares = shares[i].shareQuantity;

                        double myBuyPrice = 0.0 ;
                        if ( numShares > 0 && SellAskMap.containsKey(company)){
                            myBuyPrice = SellAskMap.get(company);
                            if ( myBuyPrice < maxBid && maxBid > 0 && numShares > 0){
                                pout.println("ASK "+company+" "+maxBid+" "+numShares);
                                pout.flush();
                            }
                        } else if (numShares > 0) {
                            myBuyPrice = SellAskMap.get(company);
                            pout.println("CLEAR_ASK "+company);
                            pout.flush();
                            System.out.println(bin.readLine());
                            double mySellPrice = myBuyPrice * 1.2;
                            if (shares[i].sellStock(mySellPrice) && mySellPrice > 0 && numShares > 0){
                                //good to call sell stock
                                pout.println("ASK "+company+" "+mySellPrice+" "+numShares);
                                pout.flush();
                                System.out.println(bin.readLine());
                            }
                        }
                    }

                    pout.println("CLOSE_CONNECTION");
                    pout.flush();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } finally {
                    pout.println("CLOSE_CONNECTION");
                    pout.flush();
                    pout.close();
                    try {
                        bin.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        };

        ScheduledExecutorService executorSellMaps = Executors.newScheduledThreadPool(1);
        executorSellMaps.scheduleAtFixedRate(SellMaps, 0, 10, TimeUnit.MILLISECONDS);

        Runnable BidMaps = new Runnable() {
            public void run() {
                Socket socket = null;
                PrintWriter pout = null;
                BufferedReader bin =null;
                try {
                    socket = new Socket("codebb.cloudapp.net", 17429);
                    pout = new PrintWriter(socket.getOutputStream());
                    bin = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    pout.println("bloomfilter" + " " + "abcd1234");
                    pout.flush();

                    int i = 0;
                    for(String company:companies){
                        shares[i].getShareInfo(pout,bin);
                        int numShares = shares[i].shareQuantity;
                        if (numShares > 0 && shares[i].isWorstPerforming()){
                            double sellPrice = shares[i].getBidValue();
                            pout.println("ASK " + company + " " + sellPrice + " " + numShares);
                            pout.flush();
                            System.out.println(bin.readLine());

                        } else if (numShares > 0){
                            double prevSellPrice = shares[i].checkAndClearExistingOrder(pout, bin);
                            double myBuyPrice = SellAskMap.get(company);
                            if (shares[i].isDecreasing() && ( prevSellPrice - myBuyPrice ) < myBuyPrice * 0.03 ) {
                                prevSellPrice = myBuyPrice - 0.10;
                            } else {
                                prevSellPrice -= 0.05;
                            }
                            if (prevSellPrice > 0 && numShares > 0) {
                                pout.println("ASK " + company + " " + prevSellPrice + " " + numShares);
                                pout.flush();
                                System.out.println(bin.readLine());
                            }
                        } else if (shares[i].isBestPerforming()){
                            shares[i].checkAndClearExistingOrder(pout, bin);
                        }
                    }

                    Arrays.sort(shares, new ShareComparator());
                    System.out.println("SortedArray");
                    for(Share share:shares){
                        System.out.println(share.company+" "+share.diffNetWorth);
                    }
                    if (shares[0].isBestPerforming()) {
                        for (int j = companies.length - 1; j >= companies.length - 5; j++) {
                            shares[j].sellEverything(pout, bin);
                        }
                        pout.println("MY_CASH");
                        pout.flush();
                        Double totalCash = Double.parseDouble(bin.readLine().toString().split(" ")[1]);
                        Double minAsk = shares[0].getAskValue();
                        int totalShares = (int) (totalCash*0.1/minAsk);
                        if(totalShares <= 0){
                            minAsk = shares[1].getAskValue();
                            totalShares = (int) (totalCash*0.1/minAsk);
                            pout.println("BID "+shares[1].company+" "+minAsk+" "+totalShares);
                            pout.flush();
                            System.out.println(bin.readLine());
                        } else {
                            pout.println("BID "+shares[0].company+" "+minAsk+" "+totalShares);
                            pout.flush();
                            System.out.println(bin.readLine());
                        }
                    }

                    pout.println("CLOSE_CONNECTION");
                    pout.flush();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } finally {
                    pout.println("CLOSE_CONNECTION");
                    pout.flush();
                    pout.close();
                    try {
                        bin.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        };

        ScheduledExecutorService executorBidMaps = Executors.newScheduledThreadPool(1);
        executorBidMaps.scheduleAtFixedRate(BidMaps, 0, 10, TimeUnit.SECONDS);
    }
}

class ShareComparator implements Comparator<Share> {

    @Override
    public int compare(Share o1, Share o2) {
        return (o1.diffNetWorth < o2.diffNetWorth? 1: (o1.diffNetWorth > o2.diffNetWorth? -1:0)); //reverse sort
    }
}