package taste;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@Test
@SuppressWarnings("unchecked")
public class TestByob {

    private Method codePath;

    @BeforeSuite
    public void setUp() throws NoSuchMethodException {
        codePath = BYOB.class.getMethod(
            "inviteesJava8"/*"inviteesTraditional"*/, new Class[]{});
    }

    public void testNoParty() throws InvocationTargetException, IllegalAccessException {
        assertFalse(((Stream<BYOB.Node>) codePath.invoke(new BYOB(null))).findAny().isPresent(),
            "No party should mean no invitees");
    }

    public void testPartyAlone() throws InvocationTargetException, IllegalAccessException {
        BYOB.Node self = new BYOB.Node(1L, 6, null);

        Optional<BYOB.Node> firstResult = ((Stream<BYOB.Node>) codePath.invoke(new BYOB(self))).findFirst();
        assertTrue(firstResult.isPresent(), "There should be at least one");
        assertEquals(firstResult.get(), self, "Can only party on your own");
    }

    public void testPartyOfTwo() throws InvocationTargetException, IllegalAccessException {
        BYOB.Node self = new BYOB.Node(1L, 6, null);
        BYOB.Node assistant = new BYOB.Node(2L, 2, null);
        self.addSubordinate(assistant);

        Stream<BYOB.Node> result = (Stream<BYOB.Node>) codePath.invoke(new BYOB(self));
        assertEquals(result.count(), 1, "Should be a party of one");
        Optional<BYOB.Node> firstResult = ((Stream<BYOB.Node>) codePath.invoke(new BYOB(self))).findFirst();
        assertEquals(firstResult.get().getEmployeeId(), 1L, "Expected employee partying should be with ID 1");
        assertEquals(firstResult.get(), self, "Better party on your own");

        BYOB.Node greedySelf = new BYOB.Node(1L, 3, null);
        BYOB.Node generousAssistant = new BYOB.Node(2L, 12, null);
        greedySelf.addSubordinate(generousAssistant);

        result = (Stream<BYOB.Node>) codePath.invoke(new BYOB(greedySelf));
        assertEquals(result.count(), 1, "Should be a party of one");
        firstResult = ((Stream<BYOB.Node>) codePath.invoke(new BYOB(greedySelf))).findFirst();
        assertEquals(firstResult.get().getEmployeeId(), 2L, "Expected employee partying should be with ID 2");
        assertEquals(firstResult.get(), generousAssistant, "Better party on her own");
    }

    public void testSkipTwoLevels() throws InvocationTargetException, IllegalAccessException {
        BYOB.Node ceo = new BYOB.Node(1L, 6, null);
        BYOB.Node svp = new BYOB.Node(2L, 2, null);
        ceo.addSubordinate(svp);
        BYOB.Node director = new BYOB.Node(3L, 2, null);
        svp.addSubordinate(director);
        BYOB.Node manager = new BYOB.Node(4L, 5, null);
        director.addSubordinate(manager);

        Stream<BYOB.Node> result = (Stream<BYOB.Node>) codePath.invoke(new BYOB(ceo));
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
    public void testBranchedOut() throws InvocationTargetException, IllegalAccessException {
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

        assertEquals(((Stream<BYOB.Node>) codePath.invoke(new BYOB(ceo)))
                .collect(Collectors.summarizingInt(BYOB.Node::getBeers)).getSum(), 17,
            "The most fun party here will have 17 beers");
    }
}