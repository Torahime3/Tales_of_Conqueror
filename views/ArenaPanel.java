package views;

import controller.Arena;
import controller.handler.KeyHandler;
import controller.manager.AnimationManager;
import controller.manager.FighterClasseManager;
import models.fighters.Fighter;
import views.dialog.DialogActions;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class ArenaPanel extends JPanel {

    Image background;
    Image profile_background;
    Arena arena;
    DialogActions da;
    GamePanel gamePanel;
    KeyHandler keyHandler;
    boolean endGame = false;
    long initAnimation = 0;
    long currentTime = 0;

    boolean fighter1DamageTaken = false;
    boolean fighter2DamageTaken = false;

    private final String classePlayer;
    private final String classeEncounter;

    public ArenaPanel(Arena arena, DialogActions da, GamePanel gamePanel, String classePlayer, String classeEncounter) {
        this.da = da;
        this.arena = arena;
        this.gamePanel = gamePanel;
        this.keyHandler = gamePanel.keyHandler;
        this.classePlayer = classePlayer;
        this.classeEncounter = classeEncounter;
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        loadRessources();

    }

    public void loadRessources() {

        try {
            background = ImageIO.read(Objects.requireNonNull(this.getClass().getResourceAsStream("/textures/fight/background-fight.png")));
            profile_background = ImageIO.read(Objects.requireNonNull(this.getClass().getResourceAsStream("/textures/fight/background-profile.png")));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void drawInfoBar(Graphics2D g2, Fighter fighter, int x){
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //NAME
        g2.setColor(Color.white);
        g2.setFont(new Font("Courier", Font.BOLD, 20));
        g2.drawString(fighter.getName(), x - 20, 210);
        //LEVEL
        g2.setColor(Color.YELLOW);
        g2.drawString("Lv." + fighter.getLevel(), x + 90, 210);
        //TYPE
        g2.setColor(fighter.getWeapon().getType().getColor());
        g2.fillRoundRect(x - 20, 235, fighter.getWeapon().getType().getName().length() * 10 + 3, 15, 5, 5);
        g2.setFont(new Font("Courier", Font.BOLD, 12));
        g2.setColor(Color.WHITE);
        g2.drawString(fighter.getWeapon().getType().getName(), x - 10, 247);
        //HP
        if(fighter.getHp() < 50) g2.setColor(Color.YELLOW);
        if(fighter.getHp() < 20) g2.setColor(Color.RED);
        g2.drawString("Hp." + fighter.getHp() + "/" + fighter.getMaxHp(), x + 70, 245);
        //HP BAR
        g2.setColor(Color.RED);
        g2.fillRoundRect(x - 20, 220, 150, 8, 20, 30);
        g2.setColor(Color.GREEN);
        g2.fillRoundRect(x - 20, 220, (int) (150 * ((float) fighter.getHp() / fighter.getMaxHp())), 8, 20, 30);
    }

    public void moveToTarget(Graphics2D g2, Fighter fighter, String classe, int startX, boolean reversed){

        long time = System.currentTimeMillis() - fighter.getWalkingTime();
        float timeInSec = time / 1300f;
        int x = startX + (int)(370 * Math.min(1, timeInSec)) * (reversed?-1:1);


        if(timeInSec >= 1 && timeInSec <= 1.3) {
            if(reversed){
                this.fighter1DamageTaken = true;
            } else {
                this.fighter2DamageTaken = true;
            }
            Objects.requireNonNull(FighterClasseManager.returnRightAnimation(classe, "attack")).paint(g2, x, 270, 96, 96, reversed);
            drawInfoBar(g2, fighter, x);
        } else if (timeInSec < 1){
            Objects.requireNonNull(FighterClasseManager.returnRightAnimation(classe, "walk")).paint(g2, x, 270, 96, 96, reversed);
            drawInfoBar(g2, fighter, x);
        } else {
            arena.applyAttack(g2);
            if(reversed){
                this.fighter1DamageTaken = false;
            } else {
                this.fighter2DamageTaken = false;
            }
            int reversedX = startX + (int) (370 * Math.max(0, 1 - (timeInSec - 1.3))  * (reversed?-1:1));
            Objects.requireNonNull(FighterClasseManager.returnRightAnimation(classe, "walk")).paint(g2, reversedX, 270, 96, 96, !reversed);
            drawInfoBar(g2, fighter, reversedX);
            if(reversedX == startX){
                arena.switchTurn();
                fighter.setWalkingTime(0);
            }
        }

    }

    public void quitArena() {
        if (initAnimation == 0) {
            initAnimation = System.currentTimeMillis();
        }
        currentTime = System.currentTimeMillis();
        if (currentTime >= initAnimation + 1500) {
            endGame = false;
            keyHandler.overWorld = true;
            initAnimation = 0;
            gamePanel.playerFighter.restoreHpMax();
            gamePanel.worldPanel.fighterEncountered.restoreHpMax();
            gamePanel.worldPanel.fighterEncountered = null;
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        //BACKGROUND
        g.drawImage(background, 0, 0, null);

        g.setColor(Color.ORANGE);

        //ANIMATION
        //JOUEUR 1
        if (arena.getFighter1().isDead()) {
            Objects.requireNonNull(FighterClasseManager.returnRightAnimation(classePlayer, "dead")).paint(g2, 100, 270, 96, 96, false);
            drawInfoBar(g2, arena.getFighter1(), 100);
        } else if (arena.getFighter1().getWalkingTime() != 0) {
            moveToTarget(g2, arena.getFighter1(), classePlayer, 100, false);
        } else {
            if(!endGame && !this.fighter1DamageTaken) Objects.requireNonNull(FighterClasseManager.returnRightAnimation(classePlayer, "idle")).paint(g2, 100, 270, 96, 96, false);
            drawInfoBar(g2, arena.getFighter1(), 100);
        }

        //JOUEUR 2
        if (arena.getFighter2().isDead()) {
            Objects.requireNonNull(FighterClasseManager.returnRightAnimation(classeEncounter, "dead")).paint(g2, 550, 270, 96, 96, true);
            drawInfoBar(g2, arena.getFighter2(), 550);
        } else if (arena.getFighter2().getWalkingTime() != 0) {
            moveToTarget(g2, arena.getFighter2(), classeEncounter, 550, true);
        } else {
            if(!endGame && !this.fighter2DamageTaken) Objects.requireNonNull(FighterClasseManager.returnRightAnimation(classeEncounter, "idle")).paint(g2, 550, 270, 96, 96, true);
            drawInfoBar(g2, arena.getFighter2(), 550);
        }

        if(this.fighter1DamageTaken){
            Objects.requireNonNull(FighterClasseManager.returnRightAnimation(classePlayer, "hit")).paint(g2, 100, 270, 96, 96, false);
        } else if (this.fighter2DamageTaken){
            Objects.requireNonNull(FighterClasseManager.returnRightAnimation(classeEncounter, "hit")).paint(g2, 550, 270, 96, 96, true);
        }

        da.setInteraction(arena.getFighter1().getWalkingTime() == 0
                && arena.getFighter2().getWalkingTime() == 0
                && !arena.getFighter1().isDead()
                && !arena.getFighter2().isDead()
                && arena.isYourTurn());

        if(!arena.getFighter1().isDead() && arena.getFighter2().isDead()){
            da.setDialogText(arena.getFighter1().getName() + " won !");
            if(arena.getFighter1().getWalkingTime() == 0){
                endGame = true;
                Objects.requireNonNull(FighterClasseManager.returnRightAnimation(classePlayer, "jump")).paint(g2, 100, 270, 96, 96, false);
                quitArena();
            }
        }
        if(!arena.getFighter2().isDead() && arena.getFighter1().isDead()){
            da.setDialogText(arena.getFighter2().getName() + " won !");
            if(arena.getFighter2().getWalkingTime() == 0){
                endGame = true;
                Objects.requireNonNull(FighterClasseManager.returnRightAnimation(classeEncounter, "jump")).paint(g2, 550, 270, 96, 96, true);
                quitArena();
            }
        }
    }
}

