/**
 *  Copyright (c) 1999-2010, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package samples.multicostregular.asap.data.base;

import samples.multicostregular.asap.data.ASAPItemHandler;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Mail: julien.menana{at}emn.fr
 * Date: Dec 16, 2008
 * Time: 4:14:21 PM
 */
public class ASAPEmployee {

    String id;
    String name;
    ASAPContract contract;
    ASAPItemHandler handler;
    HashSet<ASAPSkill> skills;




    public ASAPEmployee(ASAPItemHandler handler, String id)
    {
        this.id = id;
        this.skills = new HashSet<ASAPSkill>();
        this.handler = handler;
        handler.putEmployee(this.id,this);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ASAPContract getContract() {
        return contract;
    }

    public void setContract(ASAPContract contract) {
        this.contract = contract;
    }

    public void setContract(String cId)
    {
        this.contract = handler.getContract(cId);
    }

    public String toString()
    {
        return this.id+" ("+contract.getId()+")";
    }


    public HashSet<ASAPSkill> getSkills() {
        return skills;
    }

    public void addSkill(String skillId) {
        this.skills.add(handler.getSkill(skillId));
    }
    public void addSkill(ASAPSkill skill)
    {
        this.skills.add(skill);
    }

   

}