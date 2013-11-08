package choco.kernel.common;

import java.util.Collection;

public class DottyBean implements IDotty {

	public IDotty[] objects;
		
	
	public DottyBean() {
		super();
	}

	public DottyBean(IDotty... objects) {
		super();
		this.objects = objects;
	}


	public DottyBean(Collection<? extends IDotty> objects) {
		super();
		this.objects = objects.toArray(new IDotty[objects.size()]);
	}

	
	public final IDotty[] getObjects() {
		return objects;
	}

	
	public final void setObjects(IDotty[] objects) {
		this.objects = objects;
	}

	@Override
	public String toDotty() {
		StringBuilder b = new StringBuilder();
		for (IDotty o : objects) {
			b.append(o.toDotty()).append("\n");
		}
		return b.toString();
	}

}
