/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package positions;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author earlie87
 */
public class TradeRequestList {
    private Semaphore listSemaphore = new Semaphore(1);
    List<TradeRequestData> tradeReqList = new ArrayList<TradeRequestData>();
    private int nextId = 0;
    private int rdNextId = 0;
    private double totalMoneySaved = 0.0;
    private int reqCnt = 0;
    private int fillCnt = 0;
    private boolean areWeAllDone = false;
    public void addOne(TradeRequestData d){
        listSemaphore.acquire();
        d.setId(nextId);
        d.setStatus(TradeRequestData.TradeStates.oIdle);
        tradeReqList.add(d);
        if(++nextId > this.sizeOf()){
            nextId = 0;
        }
        listSemaphore.release();
    }
    public TradeRequestData getOne (int index){
        TradeRequestData trd = null;
        listSemaphore.acquire();
        if (index < tradeReqList.size()){
            trd = tradeReqList.get(index);
        }
        listSemaphore.release();
        
        return trd;
    }
    public void replaceOne (int index, TradeRequestData trdin){
        
        listSemaphore.acquire();
        if (index < tradeReqList.size()){
            tradeReqList.add(index, trdin);            
        }
        listSemaphore.release();        
    }
    public TradeRequestData findOne(int byId){
        int x = 0;
        boolean foundIt = false;
        listSemaphore.acquire();
        for (x = 0; ((x < tradeReqList.size()) && !foundIt); x++){
            if(tradeReqList.get(x).getId() == byId){
                foundIt = true;
            }
        }
        listSemaphore.release();
        if (foundIt == true){
            return tradeReqList.get(x);
        }else{
            return null;
        }
    }
    public TradeRequestData findNext(){
        
        boolean foundIt = false;
        TradeRequestData ret = null;
        
        if(rdNextId < tradeReqList.size()){
            listSemaphore.acquire();
            ret = tradeReqList.get(rdNextId++);
            listSemaphore.release();            
        }else{
            rdNextId = 0;
            if(this.sizeOf() > 0){
                listSemaphore.acquire();
                ret = tradeReqList.get(rdNextId++);
                listSemaphore.release();
            }            
        }                   
        return ret;
        
    }
    public TradeRequestData findOne(String byTicker){
        int x = 0;
        boolean foundIt = false;
        listSemaphore.acquire();
        for (x = 0; ((x < tradeReqList.size()) && !foundIt); x++){
            if(tradeReqList.get(x).getTicker().equals(byTicker)){
                foundIt = true;
            }
        }
        listSemaphore.release();
        if (foundIt == true){
            return tradeReqList.get(x-1);
        }else{
            return null;
        }
    }
    public boolean areAllFilled(){
        /*
        called when trader is finished and we need to wait till all trades have completed i.e filled.
        */
        int x = 0;
        boolean allFilled = true;
        listSemaphore.acquire();
        for (x = 0; ((x < tradeReqList.size()) && (allFilled == true)); x++){
            if(tradeReqList.get(x).getStatus().equals(TradeRequestData.TradeStates.oFilled)){
                allFilled = true;
            }else{
                allFilled = false;
            }
        }
        listSemaphore.release();
        return allFilled;
    }
    public TradeRequestData findOne(TradeRequestData.TradeStates byState){
        int x = 0;
        boolean foundIt = false;
        listSemaphore.acquire();
        for (x = 0; ((x < tradeReqList.size()) && !foundIt); x++){
            if(tradeReqList.get(x).getStatus() == byState){
                foundIt = true;
            }
        }
        listSemaphore.release();
        if (foundIt == true){
            return tradeReqList.get(x-1);
        }else{
            return null;
        }
    }
    public void updateTotalSaved(double sin){
        totalMoneySaved += sin;
    }
    public double getTotalMoneySaved(){
        return totalMoneySaved;
    }
    public int sizeOf(){
        return tradeReqList.size();
    }
    public void acquireSem(){
        listSemaphore.acquire();
    }
    public void releaseSem(){
        listSemaphore.release();
    }
    public int getReqCnt(){
        return reqCnt;
    }
    public int getFillCnt(){
        return fillCnt;
    }
    public void bumpReqCnt(){
        reqCnt++;
    }
    public void bumpFillCnt(){
        fillCnt++;
    }
    public void setAreWeAllDone(boolean din){
        areWeAllDone = din;
    }
    public boolean areWeAllDone(){
        return areWeAllDone;
    }
    public boolean areWeAllFilled(){
        int x = 0;
        boolean allFilled = true;
        
        for(x = 0; (x < this.sizeOf() && allFilled == true); x++){
            if(!this.getOne(x).getStatus().equals(TradeRequestData.TradeStates.oFilled))
                allFilled = false; 
            else;
        }
        return allFilled;
    }
}
