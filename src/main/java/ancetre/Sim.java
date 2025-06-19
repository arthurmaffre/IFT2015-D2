/*
 * MIT License
 * 
 * Copyright (c) 2025 Miklós Csűrös
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package pedigree;

public class Sim implements Comparable<Sim>{
    private static int SIM_COUNT=0;
    
    public static double MIN_MATING_AGE_F = 16.0; 
    public static double MIN_MATING_AGE_M = 16.0; 
    public static double MAX_MATING_AGE_F = 40.0; // Janet Jackson: 50
    public static double MAX_MATING_AGE_M = 65.0; // Al Pacino: 83
    
    /** 
     * Ordering by death date.
     * 
     * @param o
     * @return 
     */
    @Override
    public int compareTo(Sim o) {
        return Double.compare(this.deathtime,o.deathtime);
    }
    
    public enum Sex {F, M};

    private final int sim_ident;
    private double birthtime;
    private double deathtime;
    private Sim mother;
    private Sim father;
    private Sim mate;
    
    private Sex sex;
    
    /**
     * 
     * New Sim with birth date and parents
     * @param mother
     * @param father
     * @param birth
     * @param sex
     */
    protected Sim(Sim mother, Sim father, double birth, Sex sex){
        this.mother = mother;
        this.father = father;
        
        this.birthtime = birth;
        this.deathtime = Double.POSITIVE_INFINITY;
        
        this.sex = sex;
        
        this.sim_ident = SIM_COUNT++;
    }
    
    /**
     * A founding Sim.
     * 
     */
    public Sim(Sex sex) {
        this(null, null, 0.0, sex);
    }
    
    /**
     * If this sim is of mating age at the given time
     * 
     * @param time
     * @return true if alive, sexually mature and not too old
     */
    public boolean isMatingAge(double time){
        if (time<getDeathTime()) {
            double age = time-getBirthTime();
            return 
                    Sex.F.equals(getSex())
                    ? age>=MIN_MATING_AGE_F && age <= MAX_MATING_AGE_F
                    : age>=MIN_MATING_AGE_M && age <= MAX_MATING_AGE_M;
        } else
            return false; // no mating with dead people
    }
    
    /**
     * Test for having a (faithful and alive) partner. 
     * 
     * @param time
     * @return 
     */
    public boolean isInARelationship(double time){
        return mate != null && mate.getDeathTime()>time 
                && mate.getMate()==this;
    }
    
    public void setDeath(double death){
        this.deathtime = death;
    }
    
    public Sex getSex(){
        return sex;
    }
    
    public double getBirthTime(){
        return birthtime;
    }
    
    public double getDeathTime(){
        return deathtime;
    }
    
    public void setDeathTime(double death_time){
        this.deathtime = death_time;
    }
    
    /**
     * 
     * @return null for a founder
     */
    public Sim getMother(){
        return mother;
    }
    
    /**
     * 
     * @return null for a founder
     */
    public Sim getFather(){
        return father;
    }
    
    public Sim getMate(){
        return mate;
    }
    
    public void setMate(Sim mate){this.mate = mate;}
    
    public boolean isFounder(){
        return (mother==null && father==null);
    }
    
    private static String getIdentString(Sim sim){
        return sim==null?"":"sim."+sim.sim_ident+"/"+sim.sex;
    }
    
    static int simCount() {return SIM_COUNT;}    
    
    @Override
    public String toString(){
        return getIdentString(this)+" ["+birthtime+".."+deathtime+", mate "+getIdentString(mate)+"\tmom "+getIdentString(getMother())+"\tdad "+getIdentString(getFather())
        +"]";
    }
}