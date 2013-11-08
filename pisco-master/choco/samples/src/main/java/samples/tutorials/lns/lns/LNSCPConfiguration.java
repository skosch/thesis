package samples.tutorials.lns.lns;

import choco.kernel.solver.Configuration;
import parser.instances.BasicSettings;

import java.lang.reflect.Field;

import static choco.kernel.common.util.tools.PropertyUtils.logOnFailure;

/**
 * additional settings for Large Neighborhood Search based on CP
 * @author Sophie Demassey
 * @see LNSCPSolver
 */
public class LNSCPConfiguration extends Configuration {

/**
 * the limit type set on the B&B in the initial step of LNS
 * @see choco.kernel.solver.search.limit.Limit
 */
@Default(value = "BACKTRACK")
public static final String LNS_INIT_SEARCH_LIMIT = "lns.initial.cp.search.limit.type";

/** the limit value set on the B&B in the initial step of LNS */
@Default(value = "1000")
public static final String LNS_INIT_SEARCH_LIMIT_BOUND = "lns.initial.cp.search.limit.value";

/**
 * the limit type set on the backtracking in each neighborhood exploration of LNS
 * @see choco.kernel.solver.search.limit.Limit
 */
@Default(value = "BACKTRACK")
public static final String LNS_NEIGHBORHOOD_SEARCH_LIMIT = "lns.neighborhood.cp.search.limit.type";

/** the limit value set on the backtracking in each neighborhood exploration of LNS */
@Default(value = "1000")
public static final String LNS_NEIGHBORHOOD_SEARCH_LIMIT_BOUND = "lns.neighborhood.cp.search.limit.value";

/** the number of iterations of the loop in the second step of LNS */
@Default(value = "3")
public static final String LNS_RUN_LIMIT_NUMBER = "lns.run.limit.number";

/** a boolean indicating wether the CP model must be solved by LNS or B&B */
@Default(value = "true")
public static final String LNS_USE = "lns.use";

public LNSCPConfiguration()
{
	super(new BasicSettings());
}

/**
 * Load the default value of keys defined in @Default annotation
 * @param key the name of the field
 */
public String loadDefault(String key)
{
	Field[] fields = LNSCPConfiguration.class.getFields();
	for (Field f : fields) {
		try {
			if (f.get(this).equals(key)) {
				Default ann = f.getAnnotation(Default.class);
				return ann.value();
			}
		} catch (IllegalAccessException e) {
			logOnFailure(key);
		}
	}
	throw new NullPointerException("cant find ");
}

}
