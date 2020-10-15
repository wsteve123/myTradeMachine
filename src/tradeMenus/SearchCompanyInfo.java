/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tradeMenus;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

/**
 *
 * @author earlie87
 */
public class SearchCompanyInfo {
    public static enum CompanyInfoType{        
        oEarningsDate("EarningsDate"),
        oPeRatio("PeRatio");
        private String strVal = "";
        CompanyInfoType(String sin){           
            strVal = sin;
        }
    }
    URL url;
    URLConnection con;
    InputStream is;
    BufferedReader br;
    String line = null;
    private boolean open(String tickerIn) {
        boolean openSuccess = true;
        try {//https://finviz.com/quote.ashx?t=aapl
            url = new URL("https://finviz.com/quote.ashx?t=" + tickerIn);
        } catch (MalformedURLException ex) {
            openSuccess = false;
            System.out.println("\nMalformedURLException!!" + ex + "-> " + SearchCompanyInfo.class.getName());
            //Logger.getLogger(SearchCompanyInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (openSuccess == true) {
            try {
                // Get the input stream through URL Connection
                con = url.openConnection();
            } catch (IOException ex) {
                openSuccess = false;
                System.out.println("\nIOException!!" + ex + "-> " + SearchCompanyInfo.class.getName());
                //Logger.getLogger(SearchCompanyInfo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (openSuccess == true) {
			con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.29 Safari/537.36");
            try {
                is = con.getInputStream();
            } catch (IOException ex) {
                openSuccess = false;
                System.out.println("\nIOException!!" + ex + "-> " + SearchCompanyInfo.class.getName());
            //Logger.getLogger(SearchCompanyInfo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if(openSuccess == true){
            br = new BufferedReader(new InputStreamReader(is));
            line = null;
        }
        return openSuccess;
    }

    void close() throws IOException {
        is.close();
        br.close();
    }
    void SearchCompanyInfo(){        
    }

    String getInfo(String ticker, CompanyInfoType typeIn){
        String bigString = "";  
        String strResult = "";
        boolean foundIt = false;
        boolean failed = false;
        Document doc;
        Element table;
        Elements rows;
        switch (typeIn) {
            case oEarningsDate: {
                if (open(ticker) == true) {
                    try {
                        while ((line = br.readLine()) != null) {
                            bigString += (line + "\n");
                        }
                    } catch (IOException ex) {
                        //Logger.getLogger(SearchCompanyInfo.class.getName()).log(Level.SEVERE, null, ex);
                        System.out.println("\nIOException!!" + ex + "-> " + SearchCompanyInfo.class.getName());
                    }
                }else{
                    failed = true;
                }                
            }
            if (failed == true){
                break;
            }
            doc = Jsoup.parse(bigString);  
            Elements tables = doc.select("table"); //collect all tables
            for (int t = 0; (t < tables.size()) && !foundIt; t++) {                   
                table = tables.select("table").get(t); //select a table.
                rows = table.select("tr"); //collect all rows
                for (int r = 0; (r < rows.size()) && !foundIt; r++) { //cycle all rows
                    Element row = rows.get(r);
                    Elements cols = row.select("td");  //collect all columns
                    for (int c = 0; (c < cols.size()) && !foundIt; c++) { //cycle all colums
                        if(cols.get(c).text().equals("Earnings")){
                            System.out.println("\nCol.text:" + cols.get(c).text());
                            System.out.println("\nCol.text:" + cols.get(c+1).text());
                            strResult = cols.get(c+1).text();
                            foundIt = true;
                        }
                    }
                }
            }
            break;
        }
        return strResult;
    }
    private static class MyNodeVisitor implements NodeVisitor {

    private List<String> childList;

    public MyNodeVisitor(List<String> childList) {
        if (childList == null) {
            throw new NullPointerException("child cannot be null.");
        }

        this.childList = childList;
    }

    @Override
    public void head(Node node, int depth) {
        if (node.childNodeSize() == 0) {
            childList.add(node.toString());
        }
    }

    @Override
    public void tail(Node node, int depth) {

    }
    }

}
