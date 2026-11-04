package sardor.xiva.dukono.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** IDEA.md 10-bo'limidagi UzText test holatlari. */
public class UzTextTest {

    @Test
    public void normalizeLatinAndCyrillicEquivalents() {
        assertEquals("sut", UzText.normalize("Сут"));
        assertEquals("sut", UzText.normalize("sut"));

        assertEquals("ozbekiston", UzText.normalize("Ўзбекистон"));
        assertEquals("ozbekiston", UzText.normalize("O'zbekiston"));
        assertEquals("ozbekiston", UzText.normalize("Ozbekiston"));
        assertEquals("ozbekiston", UzText.normalize("O\u2018zbekiston"));

        assertEquals("kaymok", UzText.normalize("Қаймоқ"));
        assertEquals("kaymok", UzText.normalize("Qaymoq"));
        assertEquals("shakar", UzText.normalize("Шакар"));
        assertEquals("choy", UzText.normalize("Чой"));
        assertEquals("yog", UzText.normalize("Ёғ"));
        assertEquals("yangi", UzText.normalize("Янги"));
        assertEquals("etti", UzText.normalize("Етти"));
        assertEquals("etti", UzText.normalize("Yetti"));
        assertEquals("sirk", UzText.normalize("Цирк"));
        assertEquals("sirk", UzText.normalize("Tsirk"));
        assertEquals("havo", UzText.normalize("Ҳаво"));
        assertEquals("havo", UzText.normalize("Xavo"));
        assertEquals("havo", UzText.normalize("Havo"));
        assertEquals("vino", UzText.normalize("Вино"));
        assertEquals("vino", UzText.normalize("Wino"));
        assertEquals("malumot", UzText.normalize("МАЪЛУМОТ"));
        assertEquals("non 500g", UzText.normalize("Нон 500г"));
    }

    @Test
    public void toCyrillicMatchesSpec() {
        assertEquals("шакар", UzText.toCyrillic("shakar"));
        assertEquals("ўрик", UzText.toCyrillic("o'rik"));
        assertEquals("ёғ", UzText.toCyrillic("yog'"));
        assertEquals("маъно", UzText.toCyrillic("ma'no"));
        assertEquals("чой", UzText.toCyrillic("choy"));
        assertEquals("эшик", UzText.toCyrillic("eshik"));
        assertEquals("етти", UzText.toCyrillic("yetti"));
        assertEquals("цирк", UzText.toCyrillic("tsirk"));
        assertEquals("қалам", UzText.toCyrillic("qalam"));
        assertEquals("ҳовли", UzText.toCyrillic("hovli"));
        assertEquals("хамир", UzText.toCyrillic("xamir"));
        assertEquals("сут 1 литр", UzText.toCyrillic("Sut 1 litr"));
    }

    @Test
    public void sortKeyIsAlphabeticalInCyrillicOrder() {
        assertEquals(UzText.sortKey("Sut"), UzText.sortKey("Сут"));
        assertEquals(UzText.sortKey("  Sut   1 "), UzText.sortKey("сут 1"));

        String[] sorted = {
                "Анор", "Bodom", "Вино", "Gilam", "Елим2", "Ёғоч", "Non",
                "Olma", "Sut", "Shakar", "Яшил", "O'rik", "Qalam", "Hovli"
        };
        for (int i = 0; i < sorted.length - 1; i++) {
            assertTrue(
                    "Tartib buzildi: " + sorted[i] + " -> " + sorted[i + 1],
                    UzText.sortKey(sorted[i]).compareTo(UzText.sortKey(sorted[i + 1])) < 0);
        }

        assertTrue(UzText.sortKey("1 kg").compareTo(UzText.sortKey("Anor")) < 0);
    }

    @Test
    public void escapeLikeEscapesWildcards() {
        assertEquals("50\\%\\_x\\\\", UzText.escapeLike("50%_x\\"));
    }

    @Test
    public void alphabetLengthsMatch() {
        assertEquals(37, UzText.cyrLengthForTest());
        assertEquals(37, UzText.latLengthForTest());
    }
}
