// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilconcert/ilxml/iloxmlhash.h
// --------------------------------------------------------------------------
// Licensed Materials - Property of IBM
//
// 5725-A06 5725-A29 5724-Y47 5724-Y48 5724-Y49 5724-Y54 5724-Y55
// Copyright IBM Corp. 2000, 2010
//
// US Government Users Restricted Rights - Use, duplication or
// disclosure restricted by GSA ADP Schedule Contract with
// IBM Corp.
// ---------------------------------------------------------------------------

#ifndef __XML_iloxmlhashH
#define __XML_iloxmlhashH

#ifdef _WIN32
#pragma pack(push, 8)
#endif


#include <ilconcert/ilosys.h>
#include <ilconcert/iloenv.h>
#include <ilconcert/iloany.h>
#include <ilconcert/ilohash.h>

typedef IloAddressHashTable<IloInt> IdHash;

class IloXmlIdTable : public IdHash {
 public:
  IloXmlIdTable(IloEnv env, IloInt size=IloDefaultHashSize): IdHash(env, size){}
  ~IloXmlIdTable(){}
};



 /**
  * @group optim.concert.xml
  * <p> This class stores object and related IDs. </p>
  * <p> It also has an API to check the type of objects
  * if they are IloRtti objects. </p>
  */
class IloXmlObjectHandler {
private:
    IloAnyArray _link2obj;
    IloEnv _env;

    IloBool checkRttiOfObjectById(IloTypeIndex RTTI, IloRtti* exprI);
    IloBool checkTypeOfObjectById(IloTypeInfo type, IloRtti* exprI);

public:
    ~IloXmlObjectHandler();

    IloAnyArray getObjects(){
	return _link2obj;
    }

 /**
  * <p>Constructor</p>
  */
    IloXmlObjectHandler(IloEnv);

 /**
  * <p> Adds an object and a related ID. </p>
  */
    void addLink2Obj(IloInt id, const void* object);

 /**
  * <p> Empties the object. </p>
  */
    void clearLink2Obj();

 /**
  * <p> Gets an object from its ID, or returns null.</p>
  */
    IloAny getObjectById(IloInt id);

 /**
  * <p> Checks whether an object corresponds or not to the given ID. </p>
  */
    IloBool isValidId(IloInt id);

 /**
  * <p>This method checks the RTTI of the object referenced
  * by the identifier <code>Xml_Id</code> in the XML. This object must already
  * be serialized. </p>
  */
  IloBool checkRttiOfObjectById(IloTypeIndex RTTI, IloInt Xml_Id);

 /**
  * <p>This method checks the TypeInfo of the object referenced
  * by the ID in the XML. This object must have been already serialized. </p>
  */
  IloBool checkTypeOfObjectById(IloTypeInfo type, IloInt Xml_Id);

};



 /**
 * @group optim.concert.xml
 * <p> This class gives unique IDs to objects. </p>
 * <p> It also has an API to check the type of objects
 * if they are IloRtti objects. </p>
 * <p> This class is used by the IloXmlContext when it writes
 *  an object tree to XML. </p>
 * @see IloXmlContext#getIdManager()
 */
class IloXmlIdManager {
private:
    IloXmlIdTable _idHash;
    IloInt _maxId;
    IloEnv _env;

    IloBool checkRttiOfObjectById(IloTypeIndex RTTI, IloRtti* exprI);
    IloBool checkTypeOfObjectById(IloTypeInfo type, IloRtti* exprI);

public:
 IloXmlIdTable& getHash(){
    return _idHash;
 }

 /**
  * <p> Constructor.</p>
  */
    IloXmlIdManager(IloEnv env);

 /**
  * <p> Returns the IloEnv object. </p>
  */
    IloEnv getEnv() { return _env; }

 /**
  * <p> Checks whether an object has already been given an ID.
  * It does not check whether the object still exists or not. </p>
  */
    IloBool isValidId(IloInt id){
	if (_maxId >= id){
            return IloTrue;
        }
        return IloFalse;
    }

 /**
  * <p> Empties the object.</p>
  */
    void clear();

/**
 * <p>Determines whether an object has already been assigned an ID.</p>
 * <p>The method <code>firstTime</code> is set to
 * true if obj was not previously known to the IdManager; false, otherwise. </p>
 * <p> If the object has already been assigned an ID, the ID is returned
 * and firstTime is set to false;
 * otherwise, -1 is returned and firstTime is set to true. The method
 * hasId differs from getId in that it never creates an ID for an object
 * that has not already been seen by the IdManager. </p>
 */
    IloInt hasId(const void* object, IloBool& firstTime);

/**
 * <p> Returns the ID for the specified object,
 * generating a new ID if the specified object has not
 * already been identified by the IdManager. </p>
 * <p>The method <code>firstTime</code> is set to
 * true if obj was not previously known to the IdManager; false, otherwise. </p>
 */
    IloInt getId(const void* object, IloBool& firstTime);

 /**
  * <p>Returns the Maximum Id.</p>
  */
    IloInt getMaxId();


 /**
  * <p> Gets an ID for the argument.</p>
  * @see IloXmlIdManager#getId(const void*, IloBool&)
  */
    IloInt getId(const void *p) {
       IloBool dummy;
       return getId(p, dummy);
    }

 /**
  * <p>Determines whether an object has already been assigned an ID.</p>
  */
    IloBool hasId(const void *p) {
       IloBool dummy;
       return hasId(p, dummy) >= 0;
    }

    const void* getObjectById(IloInt id);

 /**
  * <p>This method checks the RTTI of the object referenced
  * by the identifier <code>Xml_Id</code> in the XML. This object must already
  * be serialized. </p>
  */
  IloBool checkRttiOfObjectById(IloTypeIndex RTTI, IloInt Xml_Id);

 /**
  * <p>This method checks the TypeInfo of the object referenced
  * by the ID in the XML. This object must have been already serialized. </p>
  */
  IloBool checkTypeOfObjectById(IloTypeInfo type, IloInt Xml_Id);


    ~IloXmlIdManager();
};


#ifdef _WIN32
#pragma pack(pop)
#endif


#endif
