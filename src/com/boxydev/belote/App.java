package com.boxydev.belote;

import com.boxydev.belote.card.Card;
import com.boxydev.belote.card.CardPackage;
import com.boxydev.belote.card.Color;
import com.boxydev.belote.gui.Board;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class App extends JFrame implements ActionListener {
    private JMenuBar menu = new JMenuBar();
    private Board board = new Board();

    private CardPackage cards;
    private Player player;
    private List<Player> bots;
    private List<Player> players;
    private Integer distributor = 0;
    private Color trump;

    public App() {
        setTitle("Belote");
        setSize(1400, 900);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setJMenuBar(menu);
        JMenuItem close = new JMenuItem("Fermer");
        menu.add(close);
        close.addActionListener(this);
        JMenuItem restart = new JMenuItem("Relancer");
        menu.add(restart);
        restart.addActionListener(this);
        setContentPane(board);
        setVisible(true);
    }

    public void run() {
        // Prepare lists
        bots = new ArrayList<>();
        players = new ArrayList<>();

        // Generate player
        player = new Player();
        player.askName();
        // player.setName("Matthieu");

        // Generate bots
        for (int i = 1; i < 4; i++) {
            bots.add(new Bot("Bot "+i, i));
        }

        // Organize player and bots in players list
        players.add(player);
        players.addAll(bots);

        // Buy a card package, mixing and cut
        // @todo Maybe ask to current distributor n for cut
        cards = new CardPackage();
        cards.mixing();
        cards.cut();

        int take = -1;
        while (take < 0) {
            // Give 3 cards next distributor
            int n = distributor;
            for (int i = 0; i < 4; i++) {
                n++;
                if (n > 3) n = 0;
                cards.distribute(players.get(n), 3);
            }

            // Give 2 cards next distributor
            n = distributor;
            for (int i = 0; i < 4; i++) {
                n++;
                if (n > 3) n = 0;
                cards.distribute(players.get(n), 2);
            }

            // Draw player on board
            board.addPlayers(players);

            // Draw display card
            Card displayCard = cards.display();
            board.displayCard(displayCard);

            // Ask for trump first round
            boolean taker = false;
            n = distributor;
            int nPlayer = 0;
            while (!taker && nPlayer < 4) {
                n++;
                if (n > 3) n = 0;
                taker = players.get(n).firstRound(n, displayCard);
                if (taker) {
                    take = n;
                    trump = displayCard.getColor();
                }
                nPlayer++;
            }

            if (!taker) {
                // Ask for trump second round
                n = distributor;
                nPlayer = 0;
                while (!taker && nPlayer < 4) {
                    n++;
                    if (n > 3) n = 0;
                    Color color = players.get(n).secondRound(n, displayCard);
                    if (color.getName() != "2") {
                        taker = true;
                        take = n;
                        trump = color;
                    }
                    nPlayer++;
                }
            }

            if (!taker) {
                // Put all cards in package
                for (int i = 0; i < 4; i++) {
                    cards.getCards().addAll(players.get(i).getCards());
                    players.get(i).getCards().clear();
                }
                cards.getCards().add(displayCard);
                displayCard = null;
                board.displayCard(displayCard);
            } else {
                // Oh ! We can play
                System.out.println(players.get(take).getName() + " prend atout " + trump);
                players.get(take).getCards().add(displayCard);
                displayCard = null;
                board.displayCard(displayCard);
            }
        }
            /*Card cardPlaying = null;
            while(cardPlaying == null) {
                cardPlaying = board.getCardPlaying();
                System.out.println(cardPlaying);
            }*/
        System.out.println("Lance l'application");
    }

    public static void main(String[] args) {
        App app = new App();
        app.run();
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getActionCommand() == "Relancer") {
            run();
        } else {
            System.exit(0);
        }
    }
}
