import java.util.Vector;

public class PCB {
    int PID;
    String PName;
    int[] resOwned; //Other_resources;
    int priority; //0-init 1-user 2-system
    int Status=-1; //(-1)-none 0-block 1-ready 2-running
    PCB next= null; //同优先级队列的下一个节点
    PCB parent= null;
    Vector<PCB> child;
    //阻塞状态的话指到所需的RCB,并且考虑所需资源数目
    RCB blocked=null;
    int NumOfRes=0;

    public PCB(String _PName,int _priority){
        PName=_PName;
        priority=_priority;
        resOwned=new int[4];
        child=new Vector<PCB>(1);
    }
    public PCB(String _PName,int _priority,PCB _parent){
        PName=_PName;
        priority=_priority;
        resOwned=new int[4];
        parent=_parent;
        child=new Vector<PCB>(1);
    }
    void blocked(RCB r,int need){
        Status=0;
        blocked=r;
        NumOfRes=need;
        next=null;//脱离就绪队列
    }
}
