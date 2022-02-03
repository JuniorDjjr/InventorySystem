SCRIPT_START
{
    LVAR_FLOAT fDrunknessTarget //In
    LVAR_FLOAT fCurrentDrunkness f
    LVAR_INT iDrunkness

    SCRIPT_NAME ISDRUNK

    IF NOT fDrunknessTarget > 0.0 //not called by external, or bad value
        TERMINATE_THIS_CUSTOM_SCRIPT
    ENDIF

    WHILE TRUE
        WAIT 0

        //PRINT_FORMATTED_NOW "%f" 1000 fCurrentDrunkness

        IF fCurrentDrunkness < fDrunknessTarget
            f = fDrunknessTarget - fCurrentDrunkness
            f *= 0.001
            f +=@ 0.0005
            fCurrentDrunkness +=@ f
        ELSE
            fCurrentDrunkness -=@ 0.0005
            fDrunknessTarget = fCurrentDrunkness
        ENDIF

        IF fCurrentDrunkness > 0.0
            f = fCurrentDrunkness
            POW f 4.0 (f)
            iDrunkness =# f
            SET_PLAYER_DRUNKENNESS 0 iDrunkness
        ELSE
            fCurrentDrunkness = 0.0
            fDrunknessTarget = 0.0
            SET_PLAYER_DRUNKENNESS 0 0
            BREAK // end script
        ENDIF
    ENDWHILE
}
SCRIPT_END