import java.util.Vector;

public class RCB {
    int RID;
    String RName;
    int totalNumber;
    int availableNumber;
    Vector<PCB> blocked;

    public RCB(int Rid,String Rname,int number){
        RID=Rid;
        RName=Rname;
        totalNumber=number;
        availableNumber=number;
        blocked= new Vector<PCB>(1);
    }
}
