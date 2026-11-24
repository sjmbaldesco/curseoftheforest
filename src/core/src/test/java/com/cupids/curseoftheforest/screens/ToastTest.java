package com.cupids.curseoftheforest.screens;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ToastTest {

    @Test
    void noticesFadeOutOnTheirOwn() {
        Toast toast = new Toast();
        toast.show("+1 coin");
        assertEquals(1, toast.size());
        toast.update(Toast.SHOW_SECONDS + 0.1f);
        assertEquals(0, toast.size());
    }

    @Test
    void aRepeatedNoticeDoesNotStack() {
        Toast toast = new Toast();
        toast.warn("The Wakwak caught you! Courage -10");
        toast.warn("The Wakwak caught you! Courage -10");
        assertEquals(1, toast.size());
        for (int i = 0; i < 5; i++) {
            toast.show("notice " + i);
        }
        assertEquals(3, toast.size()); // only the newest few stay on screen
        assertEquals("notice 4", toast.newest());
    }
}
