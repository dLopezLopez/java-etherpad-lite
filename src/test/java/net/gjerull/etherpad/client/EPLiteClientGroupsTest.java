package net.gjerull.etherpad.client;

import org.graphwalker.core.machine.ExecutionContext;
import org.graphwalker.java.annotation.GraphWalker;

@GraphWalker(value = "random(edge_coverage(100))", start = "e_setUp")
public class EPLiteClientGroupsTest extends ExecutionContext
		implements EPLiteClientGroups {

	@Override
	public void e_setUp() {
		System.out.println("**Inicializando**");

	}

	@Override
	public void e_createGroup() {
		System.out.println("**Creando Grupo**");

	}

	@Override
	public void v_GroupCreated() {
		System.out.println("Existe Grupo");
	}

	@Override
	public void e_deleteGroup() {
		System.out.println("**Borrando Grupo**");

	}

	@Override
	public void v_NoGroupCreated() {
		System.out.println("No existe Grupo");

	}

}
