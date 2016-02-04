package samples.tennis;

import java.util.*;

/**
 * if \mu = 0 and \sigma = 1, the distribution is called the standard normal distribution or the unit normal distribution denoted by N(0,1)
 * and a random variable with that distribution is a standard normal deviate.
 */
public class NormalDistributionCalculator {
    private static int sigma = 1; // > 0
    private static int mu;
//    private static int min;
//    private static int max;
//    private static int average;
//    private static final int groupsAmount = 5;
//    private static final Map<String, Object> INFO = new HashMap<>();

    public static List<ValuesGroup> splitByGroups(List<Integer> values, int groupsAmount) {
        Map<Integer, Integer> valuesMap = countValues(values);
        List<ValuesGroup> groups = new ArrayList<>();
        if (groupsAmount > valuesMap.size()) {
            throw new IllegalArgumentException("values size should not be less than groups amount, " +
                    "values size is " + values.size() + ", groupsAmount is " + groupsAmount);
        }
        int intactAmount = valuesMap.size() / groupsAmount;
        int remainder = valuesMap.size() % groupsAmount;
        int groupLevel = groupsAmount;

        Iterator<Map.Entry<Integer, Integer>> entryIterator = valuesMap.entrySet().iterator();
        for (int i = 0; i < groupsAmount && entryIterator.hasNext(); i++) {
            ValuesGroup group = new ValuesGroup();
            group.setLevel(groupLevel--);
            Map<Integer, Integer> groupValues = new TreeMap<>();
            int groupValuesSize = remainder == 0 ? intactAmount : intactAmount + 1;
            if (remainder > 0) remainder--;
            for (int j = 0; j < groupValuesSize && entryIterator.hasNext(); j++) {
                Map.Entry<Integer, Integer> entry = entryIterator.next();
                groupValues.put(entry.getKey(), entry.getValue());
            }
            group.setValues(groupValues);
            groups.add(0, group);
        }

        return groups;
    }

    private static Map<Integer, Integer> countValues(List<Integer> values) {
        Map<Integer, Integer> valuesMap = new TreeMap<>();
        for (Integer value : values) {
            Integer contained = valuesMap.get(value);
            if (contained == null) {
                valuesMap.put(value, 1);
            } else {
                valuesMap.put(value, ++contained);
            }
        }
        return valuesMap;
    }

    private static void fillInfo(List<Integer> values, long sum, Map<Integer, Integer> valuesMap) {
//        if (values.isEmpty()) {
//            return null;
//        } else if (values.size() == 1) {
//            min = average = max = values.get(0);
//        }


//        min = values.get(0);
//        max = values.get(values.size() - 1);
//        average = Math.round(sum / values.size());
//
//        INFO.put("min", min);
//        INFO.put("max", max);
//        INFO.put("average", average);
//        INFO.put("amounts", valuesMap);
    }
}

class ValuesGroup {
    private int level;
    private Map<Integer, Integer> values;

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Map<Integer, Integer> getValues() {
        return values;
    }

    public void setValues(Map<Integer, Integer> values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return "Level=" + level + ",values=" + values;
    }
}


//mu = 0, sigma = 1 => f(x | mu, sigma) = 1/Math.sqrt(2*MATH.PI)/Math.pow(Math.E, Math.pow(x,2)/2)
