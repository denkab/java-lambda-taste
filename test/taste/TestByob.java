package taste;

import org.testng.annotations.Test;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@Test
public class TestByob {

    public void testNoParty() {
        assertFalse(new BYOB(null).inviteesTraditional().findAny().isPresent(),
            "No party should mean no invitees");
    }

    public void testPartyAlone() {
        BYOB.Node self = new BYOB.Node(1L, 6, null);

        Optional<BYOB.Node> firstResult = new BYOB(self).inviteesTraditional().findFirst();
        assertTrue(firstResult.isPresent(), "There should be at least one");
        assertEquals(firstResult.get(), self, "Can only party on your own");
    }

    public void testPartyOfTwo() {
        BYOB.Node self = new BYOB.Node(1L, 6, null);
        BYOB.Node assistant = new BYOB.Node(2L, 2, null);
        self.addSubordinate(assistant);

        Stream<BYOB.Node> result = new BYOB(self).inviteesTraditional();
        assertEquals(result.count(), 1, "Should be a party of one");
        Optional<BYOB.Node> firstResult = new BYOB(self).inviteesTraditional().findFirst();
        assertEquals(firstResult.get().getEmployeeId(), 1L, "Expected employee partying should be with ID 1");
        assertEquals(firstResult.get(), self, "Better party on your own");

        BYOB.Node greedySelf = new BYOB.Node(1L, 3, null);
        BYOB.Node generousAssistant = new BYOB.Node(2L, 12, null);
        greedySelf.addSubordinate(generousAssistant);

        result = new BYOB(greedySelf).inviteesTraditional();
        assertEquals(result.count(), 1, "Should be a party of one");
        firstResult = new BYOB(greedySelf).inviteesTraditional().findFirst();
        assertEquals(firstResult.get().getEmployeeId(), 2L, "Expected employee partying should be with ID 2");
        assertEquals(firstResult.get(), generousAssistant, "Better party on her own");
    }

    public void testSkipTwoLevels() {
        BYOB.Node ceo = new BYOB.Node(1L, 6, null);
        BYOB.Node svp = new BYOB.Node(2L, 2, null);
        ceo.addSubordinate(svp);
        BYOB.Node director = new BYOB.Node(3L, 2, null);
        svp.addSubordinate(director);
        BYOB.Node manager = new BYOB.Node(4L, 5, null);
        director.addSubordinate(manager);

        Stream<BYOB.Node> result = new BYOB(ceo).inviteesTraditional();
        assertEquals(result.count(), 2, "Should be a party of two");
    }

    /* CEO                  6  (17, 16)
     *
     * SVP         2  (8, 6)          3  (8, 5)
     *
     * Dir         1  (1, 6)          5  (5, 5)
     *
     * Mgr     1   2   3            2   3
     */
    public void testBranchedOut() {
        long id = 1;
        BYOB.Node ceo = new BYOB.Node(id++, 6, null);

        BYOB.Node svp = new BYOB.Node(id++, 2, null);
        BYOB.Node director = new BYOB.Node(id++, 1, null);
        BYOB.Node manager = new BYOB.Node(id++, 1, null);
        director.addSubordinate(manager);
        manager = new BYOB.Node(id++, 2, null);
        director.addSubordinate(manager);
        manager = new BYOB.Node(id++, 3, null);
        director.addSubordinate(manager);
        svp.addSubordinate(director);
        ceo.addSubordinate(svp);

        svp = new BYOB.Node(id++, 3, null);
        director = new BYOB.Node(id++, 5, null);
        manager = new BYOB.Node(id++, 2, null);
        director.addSubordinate(manager);
        //noinspection UnusedAssignment
        manager = new BYOB.Node(id++, 3, null);
        director.addSubordinate(manager);
        svp.addSubordinate(director);
        ceo.addSubordinate(svp);

        assertEquals(new BYOB(ceo).inviteesTraditional()
                .collect(Collectors.summarizingInt(BYOB.Node::getBeers)).getSum(), 17,
            "The most fun party here will have 17 beers");
    }
}