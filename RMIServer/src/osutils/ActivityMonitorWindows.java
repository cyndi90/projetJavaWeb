/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package osutils;

import interfaces.IActivityMonitor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Activity Monitor for Windows.
 * 
 * Attention pour lancer les scripts powershell sous windows il faut lancer
 * une console powershell en administrateur et exécuter Set-ExectionPolicy
 * Unrestricted
 *
 * @author Damien
 */
public class ActivityMonitorWindows extends UnicastRemoteObject implements IActivityMonitor {
    /**
     * Command to get list of process
     */
    private static String processListCommand = "cmd.exe /c powershell.exe ../../cmd.ps1 process";

    /**
     * Command to get CPU usage
     */
    private static String cpuCommand = "cmd.exe /c powershell.exe ../../cmd.ps1 cpu";
    
    /**
     * Command to get memory usage
     */
    private static String commandMemory = "cmd.exe /c powershell.exe ../../cmd.ps1 used_mem";

    public ActivityMonitorWindows() throws RemoteException {
    }

    /**
     * Uses power shell to get list of processes running.
     * Informations per process : pid, name, cpu time and using memory.
     * @return ArrayList<Process>
     * @throws IOException
     * @throws InterruptedException 
     */
    @Override
    public ArrayList<Process> getListOfProcesses() throws IOException, InterruptedException {
        ArrayList<Process> arrayProcess = new ArrayList<>();
        java.lang.Process p = Runtime.getRuntime().exec(processListCommand);
        BufferedReader reader
                = new BufferedReader(new InputStreamReader(p.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line = "";
        while ((line = reader.readLine()) != null) {
            sb.append(line + "\n");
            String str[] = line.split("\\s+");
            String pid = "";
            String name = "";
            String mem = "";
            String proc;
            try {
                pid = str[1];
                name = str[2];
                mem = str[3];
                proc = str[4];
            } catch (ArrayIndexOutOfBoundsException e) {
                proc = "";
            }
            if (!pid.equals("")) {
                Process process = new Process(pid, name, null, proc, null, mem, null);
                arrayProcess.add(process);
            }
        }
        return arrayProcess;
    }

    /**
     * Uses power shell to retrieve memory information.
     * The command provides total, used and free memory values.
     * @return Memory
     * @throws RemoteException
     * @throws IOException 
     */
    @Override
    public Memory getPhysicalMemory() throws RemoteException, IOException {
        String used = "";
        String free = "";
        String total = "";
        
        java.lang.Process p = null;
        p = Runtime.getRuntime().exec(commandMemory);
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        StringBuilder sb = new StringBuilder();
        try {
            used = reader.readLine();
        } catch (IOException e) {
            System.out.println(e);
        }
        commandMemory = "cmd.exe /c powershell.exe ../../cmd.ps1 free_mem";
        
        p = Runtime.getRuntime().exec(commandMemory);
        
        reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        sb = new StringBuilder();
        try {
            free = reader.readLine();
        } catch (IOException e) {
            System.out.println(e);
        }
        commandMemory = "cmd.exe /c powershell.exe ../../cmd.ps1 total_mem";
        
        p = Runtime.getRuntime().exec(commandMemory);
        reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        sb = new StringBuilder();
        try {
            total = reader.readLine();
        } catch (IOException e) {
            System.out.println(e);
        }
        Memory mem = new Memory("", "", "");
        
            mem = new Memory(total, used, free);
        return mem;
    }

    /**
     * Gets CPU usage information with power shell.
     * The command gets total percentage of CPU usage.
     * @return CPU
     * @throws RemoteException
     * @throws IOException 
     */
    @Override
    public CPU getCPU() throws RemoteException, IOException {
        CPU cpuTotal = new CPU("","","","");
        
        java.lang.Process p = null;
        p = Runtime.getRuntime().exec(cpuCommand);
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line = "";
        try {
            while ((line = reader.readLine()) != null) {
                if (!line.equals("")) {
                    cpuTotal = new CPU(line,"","","");
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ActivityMonitorWindows.class.getName()).log(Level.SEVERE, null, ex);
        }
        return cpuTotal;
    }

}
