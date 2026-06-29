package com.surstudio.cts.scoring;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FlatScoringStrategyTest {

    private final ScoringStrategy strategy = new FlatScoringStrategy();

    @Test
    void allCorrect_returns100() {
        assertThat(strategy.score(4, 4)).isEqualTo(100.0);
    }

    @Test
    void noneCorrect_returns0() {
        assertThat(strategy.score(0, 4)).isEqualTo(0.0);
    }

    @Test
    void halfCorrect_returns50() {
        assertThat(strategy.score(2, 4)).isEqualTo(50.0);
    }

    @Test
    void threeOutOfFour_returns75() {
        assertThat(strategy.score(3, 4)).isEqualTo(75.0);
    }

    @Test
    void oneOutOfThree_roundsCorrectly() {
        // 1/3 = 33.333... → 33.33
        assertThat(strategy.score(1, 3)).isEqualTo(33.33);
    }

    @Test
    void zeroTotalQuestions_returns0() {
        assertThat(strategy.score(0, 0)).isEqualTo(0.0);
    }
}
