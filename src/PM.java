import java.util.*;

public class PM{
    Vector<PCB> processList; //包括所有创建过的进程
    PCB[] readyListHeads; //三个优先级的链表
    //PCB blockListHead;//阻塞的链表
    // 实际上也是四个RCB的阻塞链表，因为这里只因为缺少资源而阻塞
    int runningPriority;//readyList's head is running
    TreeMap<String,RCB> resMap = new TreeMap<>();//Rname名字到RCB的映射
    TreeMap<Integer,String> status = new TreeMap();//Status映射状态名以便用户阅读


    PCB getRunningP(){
        PCB p = readyListHeads[runningPriority];
        //某就绪队列第一个进程正在运行
        if(p.Status==2){
            return p;
        }else{
            //System.out.println("getRunningP error:no running");
            return null;
        }
    }

    void insertAtLast(PCB p){
        p.Status = 1;
        p.next=null;
        int priority=p.priority;
        PCB ptr = readyListHeads[priority];
        if(ptr==null){
            readyListHeads[priority]=p;
        }
        else{
            while(ptr.next!=null){
                ptr=ptr.next;
            }
            ptr.next=p;
        }
    }
    void preempt(PCB running,PCB toRun){
        //由于选择的toRun一定是该就绪队列第一个就绪的，所以PM的就绪队列不需调整顺序
        //PCB status变化
        if(running!=null)running.Status=1;
        toRun.Status=2;
        //PM更新runningPriority的唯一途径
        runningPriority = toRun.priority;
    }
    void scheduler(){
        PCB running=getRunningP();
        PCB highP=null;
        //找到最高优先级的就绪进程p
        for(int i=2;i>=0;i--){
            if(readyListHeads[i]==null){
                continue;
            }
            else{
                if(readyListHeads[i].Status==2 && readyListHeads[i].next!=null){
                    //第一个进程正在运行，选择他的下一个就绪进程
                    highP = readyListHeads[i].next;
                    break;
                }else{
                    //第一个ready的进程 | 队列里只有这唯一一个正在运行的进程
                    highP = readyListHeads[i];
                    break;
                }
            }
        }
        //仅当没有多余进程且
        //判断三个条件，是否让p抢占
        if(runningPriority<highP.priority||running==null||running.Status!=2){
            //条件1：存在更高优先级进程
            //条件2：运行进程状态已经变化，让一个优先级次之的就绪进程代替
            //条件3：没有正在运行的进程，让优先级次之的代替
            preempt(running,highP);
            //抢占需要确保更改上一个已运行进程Status变化，并且移动他到队列最后；runningPrio也要变化
        }
        System.out.println(getRunningP().PName+" isRunning Priority："+runningPriority);
        //输出运行进程名
    }
    void init(String[] ins){
        RCB r1 = new RCB(0,"R1",1);
        RCB r2 = new RCB(1,"R2",2);
        RCB r3 = new RCB(2,"R3",3);
        RCB r4 = new RCB(3,"R4",4);
        resMap.put("R1",r1);
        resMap.put("R2",r2);
        resMap.put("R3",r3);
        resMap.put("R4",r4);
        status.put(-1,"none");
        status.put(0,"block");
        status.put(1,"ready");
        status.put(2,"running");

        PCB PInit=new PCB("root",0);
        PInit.PID=0;
        PInit.Status=2;

        processList = new Vector<PCB>(1);
        processList.add(0,PInit);//记入一张总的进程列表中
        readyListHeads= new PCB[3];
        readyListHeads[0]=PInit; //记入就绪列表 且是正在运行
        //blockListHead=null;
        runningPriority=0;
        System.out.println("Init Successfully");
    };

    void cr(String[] ins){
        PCB runningP = getRunningP();
        PCB customProgress = new PCB(ins[1],Integer.parseInt(ins[2]),runningP);
        customProgress.PID = processList.lastElement().PID+1;
        insertAtLast(customProgress);

        runningP.child.add(customProgress);
        processList.add(customProgress);
        System.out.println("Create Successfully");
        scheduler();
    };
    void de(String[] ins){
        String name=ins[1];
        Iterator<PCB> it=processList.iterator();
        while(it.hasNext()){
            PCB target = it.next();
            if(name.equals(target.PName)){
                //递归消灭进程以及子进程
                killP(target);
                //从其父节点的孩子中删去自己
                target.parent.child.remove(target);
                break;
            };
        }
        scheduler();
    };
    void req(String[] ins){
        PCB runningP=getRunningP();
        RCB r = resMap.get(ins[1]);
        int need=Integer.parseInt(ins[2]);
        if(r.availableNumber>=need){
            int index= r.RID;
            r.availableNumber -= need;
            runningP.resOwned[index]=need;
        }else{
            //block
            if(need>r.totalNumber){
                System.out.println("申请资源超过系统所有");
            }else {
                System.out.println("可用资源不足");
            }

            //PM readyList update
            readyListHeads[runningPriority]=runningP.next;
            //PCB update
            runningP.blocked(r,need);
            //RCB update
            r.blocked.add(r.blocked.size(),runningP);  //add(index,object)???

        }
        scheduler();
    };
    void rel(String[] ins){
        PCB running = getRunningP();
        RCB r = resMap.get(ins[1]);
        int releaseNum= Integer.parseInt(ins[2]);

        //释放资源，更新PCB,RCB
        if(running.resOwned[r.RID]>=releaseNum && releaseNum>0){
            running.resOwned[r.RID]-=releaseNum;
            r.availableNumber += releaseNum;
            wake(r);
        }else{
            System.out.println("释放资源数目错误");
        }

        scheduler();
    };
    void to(String[] ins){
        PCB runningP = getRunningP();
        if(runningP!=null){
            //从首位移除，插到最后
            readyListHeads[runningPriority]=runningP.next;
            insertAtLast(runningP);
        }
        scheduler();
    };
    void list(String[] ins){
        System.out.println("-----------------");
        Iterator<PCB> it = processList.iterator();
        while (it.hasNext()){
            PCB p = it.next();
            System.out.println("PID-"+p.PID+" PName-"+p.PName+" Status-"+status.get(p.Status)+" resources:"+p.resOwned[0]+p.resOwned[1]+p.resOwned[2]+p.resOwned[3]);
        }
        System.out.println("");
        Iterator<RCB> iter = resMap.values().iterator();
        while (iter.hasNext()){
            RCB r = iter.next();
            System.out.println("RName-"+r.RName+" : NowAvailable-"+r.availableNumber+"/"+r.totalNumber);
        }
        System.out.println("-----------------");
    };
    void pr(String[] ins){
        System.out.println("-----------------");
        //用户先用list获取所需的pid，再利用pid使用pr指令详细查询
        int pid = Integer.parseInt(ins[1]);
        PCB p = processList.elementAt(pid);
        System.out.println("PID: "+p.PID);
        System.out.println("PName: "+p.PName);
        System.out.println("Status: "+status.get(p.Status));
        if(p.Status==0){
            System.out.println("Require "+p.NumOfRes+" resources of "+p.blocked.RName);
        }
        System.out.println("Priority: "+p.priority);
        for(int i=0;i<4;i++)System.out.println("R"+(i+1)+" Occupied: "+p.resOwned[i]);
        System.out.println("ParentProcessPID： "+p.parent.PID);
        if(!p.child.isEmpty()){
            System.out.print("ChildProcessPID:");
            Iterator<PCB> it = p.child.iterator();
            while (it.hasNext()){
                PCB q= it.next();
                System.out.print(" "+q.PID);
            }
            System.out.println("\n-----------------");
        }else{
            System.out.println("ChildProcess: NONE");
            System.out.println("-----------------");
        }

    };

    void killP(PCB p){
        //kill所有子节点
        Iterator<PCB> it = p.child.iterator();
        while(it.hasNext()){
            killP(it.next());
        }
        //删除PM所有队列中p的信息,只在processList里保留它存在过的迹象
        if(p.Status>=1){  //ready or running
            PCB last=readyListHeads[p.priority];
            if(last == p)readyListHeads[p.priority]=p.next;
            else{
                while(last.next!=p){
                    last=last.next;
                }
                last.next=p.next;
            }
        }else{
            if(p.blocked!=null){
                p.blocked.blocked.remove(p); //删去rcb阻塞队列中的p
                p.blocked=null;
                p.NumOfRes=0;
            }
        }

        //更新PCB
        p.Status=-1;

        //释放所有资源，更新RCB
        for(int i = 0;i<4;i++){
            int res=p.resOwned[i];
            String Rname="R"+Integer.toString(i+1);
            RCB r=resMap.get(Rname);
            r.availableNumber += res;
            wake(r);
            p.resOwned[i]=0;
        }
    }
    void wake(RCB r){
        //尝试唤醒r中的阻塞进程
        //检查阻塞队列第一个，NumOfRes满足的唤醒
        while(!r.blocked.isEmpty() && r.blocked.firstElement().NumOfRes<=r.availableNumber){
            //改Status,resOwned,加入就绪队列,离开阻塞队列,重置blocked和numofres
            PCB p = r.blocked.firstElement();
            p.resOwned[r.RID] += p.NumOfRes;
            p.blocked=null;
            p.NumOfRes=0;
            r.blocked.remove(p);
            insertAtLast(p);
        }
    }
};


