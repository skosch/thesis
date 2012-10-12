// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/ilocollexpr/ilocollhelpers.h
// --------------------------------------------------------------------------
// Licensed Materials - Property of IBM
//
// 5725-A06 5725-A29 5724-Y48 5724-Y49 5724-Y54 5724-Y55 
// Copyright IBM Corp. 2000, 2011
//
// US Government Users Restricted Rights - Use, duplication or
// disclosure restricted by GSA ADP Schedule Contract with
// IBM Corp.
// ---------------------------------------------------------------------------

#ifndef __ADVANCED_ilocollhelpersH
#define __ADVANCED_ilocollhelpersH

#ifdef _WIN32
#pragma pack(push, 8)
#endif
#include <ilopl/ilosys.h>

//------------------------
template< class T >
class IloNumberComparator {
public:
	IloNum compare( T left, T right ) {
		return left - right;
	}
};

typedef IloNumberComparator< IloInt > IloIntComparator;
typedef IloNumberComparator< IloNum > IloNumComparator;

template< class T >
class IloReverseNumberComparator : public IloNumberComparator< T > {
public:
	IloNum compare( T left, T right ) {
		return -1 * IloNumberComparator< T >::compare( left, right );
	}
};

typedef IloReverseNumberComparator< IloInt > IloReverseIntComparator;
typedef IloReverseNumberComparator< IloNum > IloReverseNumComparator;

class IloSymbolComparator {
public:
	IloNum compare( IloAny left, IloAny right ) {
		return strcoll( ((IloSymbolI*)left)->getString(), ((IloSymbolI*)right)->getString() );
	}
};

class IloReverseSymbolComparator : public IloSymbolComparator {
public:
	IloNum compare( IloAny left, IloAny right ) {
		return -1 * IloSymbolComparator::compare( left, right );
	}
};


static IloIntComparator intComparator;
//static IloReverseIntComparator reverseIntComparator;

static IloNumComparator numComparator;
//static IloReverseNumComparator reverseNumComparator;

static IloSymbolComparator symbolComparator;
//static IloReverseSymbolComparator reverseSymbolComparator;


//------------------------
class IloElementComparator : public IloEnvObjectI {
public:
	IloElementComparator(IloEnvI* env) : IloEnvObjectI(env){}
	
	virtual IloNum compare( IloElementComparator* c ) const = 0;
	virtual IloInt getSize() const = 0;
	virtual ~IloElementComparator(){}
};

class IloSymbolElementComparator : public IloElementComparator {
public:
	virtual ~IloSymbolElementComparator(){}
	IloSymbolElementComparator( IloEnvI* env, IloAny value ) : IloElementComparator(env), _value( (IloSymbolI*)value ) {}
	IloSymbolElementComparator( IloEnvI* env,  IloSymbol value ) : IloElementComparator(env), _value( value ) {}

	virtual IloNum compare( IloElementComparator* c ) const {		
		return strcoll( _value.getString(), ((IloSymbolElementComparator*)c)->getValue().getString() );
	}

	virtual IloNum compare( IloAny c ) const {		
		return strcoll( _value.getString(), ((IloSymbolI*)c)->getString() );
	}

	virtual IloInt getSize() const { return sizeof( IloSymbolComparator ); }

	void setValue( IloSymbol value ) { _value = value; }
	IloSymbol getValue() const { return _value; }

private:
	IloSymbol _value;
};

class IloReverseSymbolElementComparator : public IloSymbolElementComparator {
public:
	virtual ~IloReverseSymbolElementComparator(){}
	IloReverseSymbolElementComparator( IloEnvI* env, IloAny value ) : IloSymbolElementComparator(env, value ) {}
	IloReverseSymbolElementComparator( IloEnvI* env, IloSymbol value ) : IloSymbolElementComparator(env, value ) {}

	virtual IloNum compare( IloElementComparator* c ) const {		
		return -1 * IloSymbolElementComparator::compare( c );
	}

	virtual IloNum compare( IloAny c ) const {		
		return -1 * IloSymbolElementComparator::compare( c );
	}
};

//------------------------
template< class T >
class IloNumberElementComparator : public IloElementComparator {
public:	
	virtual ~IloNumberElementComparator(){}

	IloNumberElementComparator(IloEnvI* env, T value ) : IloElementComparator(env), _value( value ) {}

	virtual IloNum compare( IloElementComparator* c ) const {
		return compare( ((IloNumberElementComparator<T>*)c)->getValue() );
	}

	virtual IloNum compare( T c ) const {
		return _value - c;
	}

	virtual IloInt getSize() const { return sizeof( IloNumberElementComparator< T > ); }

	void setValue( T value ) { _value = value; }
	T getValue() const { return _value; }

private:
	T _value;
};

template< class T >
class IloReverseNumberElementComparator : public IloNumberElementComparator< T > {
public:
	virtual ~IloReverseNumberElementComparator(){}
	IloReverseNumberElementComparator(IloEnvI* env,  T value ) : IloNumberElementComparator< T >(env, value ) {}
	virtual IloNum compare( IloElementComparator* c ) const {
		return -1 * IloNumberElementComparator<T>::compare( c );
	}
	virtual IloNum compare( T c ) const {
		return -1 * IloNumberElementComparator<T>::compare( c );
	}
};

typedef IloNumberElementComparator< IloInt > IloIntElementComparator;
typedef IloNumberElementComparator< IloNum > IloNumElementComparator;
typedef IloReverseNumberElementComparator< IloInt > IloReverseIntElementComparator;
typedef IloReverseNumberElementComparator< IloNum > IloReverseNumElementComparator;

//-----------------------
class IloTupleElementComparator : public IloElementComparator {
public:
	virtual ~IloTupleElementComparator(){}
	
	IloTupleElementComparator(IloEnvI* env, const IloTuple& value ) : IloElementComparator(env), _value( value ) {}

	virtual IloNum compare( IloElementComparator* c ) const {
		return compare( ((IloTupleElementComparator*)c)->getValue().getImpl() );
	}

	virtual IloNum compare( const IloTupleI* tuple ) const {

		IloTupleSchemaI* schema = getValue().getImpl()->getSchema();

		IloIntArray keys = schema->getOrMakeTotalKeyIndexes();		

		IloInt idx = -1;
		IloNum isEquals = 0;
		
		IloInt nbKeys = keys.getSize();

		for( IloInt i = 0; i < nbKeys; ++i ) {

			idx = keys[ i ];

			if( schema->isInt( idx ) ) {				
				isEquals = intComparator.compare( _value.getIntValue( idx ), tuple->getIntValue( idx ) );

			} else if( schema->isNum( idx ) ) {
				isEquals = numComparator.compare( _value.getNumValue( idx ), tuple->getNumValue( idx ) );

			} else if( schema->isSymbol( idx ) ) {
				isEquals = symbolComparator.compare( _value.getSymbolValue( idx ).getImpl(), tuple->getSymbolValue( idx ).getImpl() );
			}

			if( !isEquals ) {
				return isEquals;
			}
		}
		return 0;	// this == c
	}

	virtual IloInt getSize() const { return sizeof( IloTupleElementComparator ); }

	void setValue( const IloTuple& value ) { _value = value; }
	IloTuple getValue() const { return _value; }

private:
	IloTuple _value;
};

class IloReverseTupleElementComparator : public IloTupleElementComparator {
public:
	virtual ~IloReverseTupleElementComparator(){}
	
	IloReverseTupleElementComparator(IloEnvI* env, const IloTuple& value ) : IloTupleElementComparator(env, value ) {}

	virtual IloNum compare( IloElementComparator* c ) const {
		return -1 * IloTupleElementComparator::compare( c );
	}

	virtual IloNum compare( const IloTupleI* tuple ) const {
		return -1 * IloTupleElementComparator::compare( tuple );
	}
};

class IloTupleCellArrayElementComparator : public IloElementComparator {
public:
	virtual ~IloTupleCellArrayElementComparator(){}
	
	IloTupleCellArrayElementComparator(IloEnvI* env, const IloTupleCellArray& value ) : IloElementComparator(env), _value( value ) {}

	virtual IloNum compare( IloElementComparator* c ) const {
		return compare( ((IloTupleCellArrayElementComparator*)c)->getValue() );
	}

	virtual IloNum compare( const IloTupleCellArray& tupleCellArray ) const {

		IloNum isEquals = 0;
		IloInt size = tupleCellArray.getSize();

		for( IloInt i = 0; i < size; ++i ) {

			IloTuplePath::IloTupleCell cell = _value[ i ];

			if( cell.isInt() ) {				
				isEquals = intComparator.compare( cell.getIntValue(), tupleCellArray[ i ].getIntValue() );

			} else if( cell.isNum() ) {
				isEquals = numComparator.compare( cell.getNumValue(), tupleCellArray[ i ].getNumValue() );

			} else if( cell.isAny() ) {
				IloAny any = cell.getAnyValue();

				if( 0 != Ilo_dynamic_cast_IloRtti< IloSymbolI >( any ) ) {
					isEquals = symbolComparator.compare( cell.getAnyValue(), tupleCellArray[ i ].getAnyValue() );
				}
			}

			if( !isEquals ) {
				return isEquals;
			}
		}
		return 0;	// this == c
	}

	virtual IloInt getSize() const { return sizeof( IloTupleCellArrayElementComparator ); }

	void setValue( const IloTupleCellArray& value ) { _value = value; }
	IloTupleCellArray getValue() const { return _value; }

private:
	IloTupleCellArray _value;
};

class IloReverseTupleCellArrayElementComparator : public IloTupleCellArrayElementComparator {
public:
	virtual ~IloReverseTupleCellArrayElementComparator(){}
	
	IloReverseTupleCellArrayElementComparator(IloEnvI* env, const IloTupleCellArray& value ) : IloTupleCellArrayElementComparator(env, value ) {}

	virtual IloNum compare( IloElementComparator* c ) const {
		return -1 * IloTupleCellArrayElementComparator::compare( c );
	}

	virtual IloNum compare( const IloTupleCellArray& tupleCellArray ) const {
		return -1 * IloTupleCellArrayElementComparator::compare( tupleCellArray );
	}
};

//-----------------------
class IloSortElement {
private:
	IloArray< IloElementComparator* > _values;
	IloInt _position;

public:
	IloSortElement() : _values( 0 ), _position( 0 ) {}
	IloSortElement( IloEnv env, IloInt size, IloInt position, IloAny  = 0 ): _values( env, size ), _position( position ){}
	~IloSortElement(){}

	void removeAll(){
		IloInt size = _values.getSize();
		for( IloInt i = 0; i < size; ++i ) {
			if( 0 != _values[ i ] ) {
				delete _values[i];
				//_values.getEnv().getImpl()->free( _values[ i ], _values[ i ]->getSize() );
			}
		}
		_values.end(); 
	}

	IloInt getPosition() const { return _position; }
	void setValue( IloInt pos, IloElementComparator* c ) { _values[ pos ] = c; }

	IloNum compare( const IloSortElement& s ) const {
		IloNum compare = 0;
		IloInt size = _values.getSize();
		for( IloInt i = 0; 
			i < size 
				&& 0 != _values[ i ]
				&& 0 == (compare = _values[ i ]->compare( s._values[ i ] ) );
			i += 1 ) {  }
		return compare;
	}

	static void sort( IloArray<IloSortElement>& elements ) {
		sort( elements, 0, elements.getSize() - 1 );
	}

	static void sort( IloArray<IloSortElement>& elements, IloInt min, IloInt max ) {
		if (max > min) {
			IloSortElement pivot = elements[(min + max) / 2];
			IloInt top = max;
			IloInt bot = min;
			while (bot <= top) {
				while (bot < max && 0 > elements[bot].compare( pivot ) ) {
					bot += 1;
				}
				while (top > min && 0 < elements[top].compare( pivot ) ) {
					top -= 1;
				}
				if (bot <= top) {
					IloSortElement tmp = elements[bot];
					elements[bot] = elements[top];
					elements[top] = tmp;
					bot += 1; 
					top -= 1;
				}
			}
			sort(elements, bot, max);
			sort(elements, min, top);
		}
	}
};


#ifdef _WIN32
#pragma pack(pop)
#endif

#endif	// __ADVANCED_ilocollhelpersH
