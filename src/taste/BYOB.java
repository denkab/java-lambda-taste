package taste;

import java.util.LinkedList;
import java.util.List;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Date: 9/9/14
 * Time: 9:26 PM
 * Copyright Denis Baranov 2012-2014
 */
public class BYOB {
    private final Node company;

    public BYOB(Node root) {
        this.company = root;
    }

    public Stream<Node> inviteesTraditional() {
        if (company == null) {
            return StreamSupport.stream(Spliterators.emptySpliterator(), false);
        }

        Bundle<Integer, Integer, Node> resultProto = beersFromSubgraph(company);

        return (resultProto.myBeers > resultProto.beersOfSubNetwork
            ? resultProto.inviteesWithManager : resultProto.inviteesWithoutManager)
                .parallelStream();
    }

    private Bundle<Integer, Integer, Node> beersFromSubgraph(Node me) {
        Bundle<Integer, Integer, Node> aggregate = new Bundle<>(me.beers, 0);
        aggregate.inviteesWithManager.add(me);

        if (!me.getSubordinates().isEmpty()) {
            for (Node sub: me.getSubordinates()) {
                Bundle<Integer, Integer, Node> bundle = beersFromSubgraph(sub);

                aggregate.myBeers = aggregate.myBeers + bundle.beersOfSubNetwork;
                aggregate.inviteesWithManager.addAll(bundle.inviteesWithoutManager);

                if (bundle.myBeers > bundle.beersOfSubNetwork) {
                    aggregate.beersOfSubNetwork = aggregate.beersOfSubNetwork + bundle.myBeers;
                    aggregate.inviteesWithoutManager.addAll(bundle.inviteesWithManager);
                } else {
                    aggregate.beersOfSubNetwork += bundle.beersOfSubNetwork;
                    aggregate.inviteesWithoutManager.addAll(bundle.inviteesWithoutManager);
                }
            }
        }

        return aggregate;
    }

    public Stream<Node> inviteesJava8() {
        return null;
    }

    public static class Node {
        private long employeeId;
        private int beers;
        private List<Node> subordinates;

        public Node(long employeeId, int beers, List<Node> foundingTeam) {
            this.employeeId = employeeId;
            this.beers = beers;
            this.subordinates = new LinkedList<>();
            if (foundingTeam != null) {
                this.subordinates.addAll(foundingTeam);
            }
        }

        public void addSubordinate(Node assistant) {
            subordinates.add(assistant);
        }

        public long getEmployeeId() {
            return employeeId;
        }

        public int getBeers() {
            return beers;
        }

        @Override
        public int hashCode() {
            return Integer.hashCode((int) employeeId);
        }

        @Override
        public boolean equals(Object obj) {
            return obj != null &&
                (this == obj ||
                    obj instanceof Node && employeeId == ((Node) obj).employeeId);
        }

        public List<Node> getSubordinates() {
            return new LinkedList<>(subordinates);
        }
    }

    private class Bundle<T1, T2, T3> {
        private T1 myBeers;
        private T2 beersOfSubNetwork;
        private List<T3> inviteesWithManager = new LinkedList<>();
        private List<T3> inviteesWithoutManager = new LinkedList<>();

        public Bundle(T1 myBeers, T2 subSubBeers) {
            this.myBeers = myBeers;
            this.beersOfSubNetwork = subSubBeers;
        }
    }
}
