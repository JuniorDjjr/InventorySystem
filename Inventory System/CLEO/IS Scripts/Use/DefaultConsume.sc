// by Junior_Djjr - MixMods.com.br
// You need: https://forum.mixmods.com.br/f141-gta3script-cleo/t5206-como-criar-scripts-com-cleo
SCRIPT_START
{
    // In: Char handle and pointer to stored item
    LVAR_INT hChar pStoredItem
    // In: Your values configured in .ini file, MUST RECEIVE AS FLOAT, if you need INT, just convert it (i =# f).
    LVAR_FLOAT fHealth fHungryMult fCalories fAnimID fThirst fDrunkness

    // Your variables definition
    LVAR_INT i iAnimID bOk bConsumed iAudio bIsDrink pDefaultHungryMultPointer iHealthBeforeEat iAudioSlot iAudioBank bHungryMaxHealthPatched
    LVAR_FLOAT f fMaxHealth fFinishAnimTime fAnimProgress fPatchedHungryMult
    LVAR_TEXT_LABEL16 tIFP tAnim

    // Constants - Prefer using them for read only

    // Item stored struct params IDs
    CONST_INT STORED_ITEM_ID 0
    CONST_INT STORED_ITEM_DATA 1
    CONST_INT STORED_COUNT 2 // prefer using ITEM_FLAG_DECREASE_COUNT
    CONST_INT STORED_FLAGS 3
    
    // Item flags
    CONST_INT ITEM_FLAG_USING 0
    CONST_INT ITEM_FLAG_DECREASE_COUNT 1
    CONST_INT ITEM_FLAG_HIDE 2

    // Item data struct param IDs
    CONST_INT DATA_ITEM_ID 0
    CONST_INT DATA_MODEL_ID 1
    CONST_INT DATA_BONE 2
    CONST_INT DATA_STACK 3

    // End script if not called by script root, or wrong char arg
    IF hChar = 0
        TERMINATE_THIS_CUSTOM_SCRIPT
    ENDIF

    // -----------------------------
    // Your code called when using the item

    IF NOT DOES_CHAR_EXIST hChar
        GOSUB StopUsing
        TERMINATE_THIS_CUSTOM_SCRIPT
    ENDIF

    GET_PED_TYPE hChar i
    IF i < 2 // is player
        SET_PLAYER_CYCLE_WEAPON_BUTTON i OFF
    ENDIF

    bIsDrink = FALSE
    iAudioSlot = -1
    iAnimID =# fAnimID

    SWITCH iAnimID
        CASE 0
            tIFP = VENDING
            tAnim = VEND_Eat_P
            fFinishAnimTime = 1.0
            iAudioBank = SOUND_BANK_RESTAURANT
            iAudio = SOUND_RESTAURANT_CJ_EAT
            BREAK
        CASE 1
            tIFP = VENDING
            tAnim = vend_eat1_P
            fFinishAnimTime = 0.7
            iAudioBank = SOUND_BANK_RESTAURANT
            iAudio = SOUND_RESTAURANT_CJ_EAT
            BREAK
        CASE 2
            tIFP = FOOD
            tAnim = EAT_Burger
            fFinishAnimTime = 0.6
            iAudioBank = SOUND_BANK_RESTAURANT
            iAudio = SOUND_RESTAURANT_CJ_EAT
            BREAK
        CASE 3
            tIFP = FOOD
            tAnim = EAT_Chicken
            fFinishAnimTime = 0.7
            iAudioBank = SOUND_BANK_RESTAURANT
            iAudio = SOUND_RESTAURANT_CJ_EAT
            BREAK
        CASE 4
            tIFP = FOOD
            tAnim = EAT_Pizza
            fFinishAnimTime = 0.9
            iAudioBank = SOUND_BANK_RESTAURANT
            iAudio = SOUND_RESTAURANT_CJ_EAT
            BREAK
        CASE 5
            tIFP = VENDING
            tAnim = VEND_Drink_P
            fFinishAnimTime = 1.0
            iAudioBank = SOUND_BANK_RESTAURANT
            iAudio = SOUND_RESTAURANT_CJ_EAT //can't use vending drink here
            bIsDrink = TRUE
            BREAK
    ENDSWITCH

    IF NOT iAudio = -1
    AND NOT iAudioBank = -1
        GOSUB FindUnusedAudioSlot
        IF NOT iAudioSlot = -1
            LOAD_MISSION_AUDIO iAudioSlot iAudioBank // I don't know if LOAD_ALL_MODELS_NOW is useful here, if not, some mod may do this in the future
        ENDIF
    ENDIF
    REQUEST_ANIMATION $tIFP
    LOAD_ALL_MODELS_NOW

    IF NOT iAudio = -1
    AND NOT iAudioSlot = -1
        timera = 0
        TASK_PLAY_ANIM_SECONDARY hChar $tAnim $tIFP 3.0 FALSE FALSE FALSE FALSE -1
		WHILE NOT HAS_MISSION_AUDIO_LOADED iAudioSlot
            WAIT 0
		ENDWHILE
        i = 700 - timera // delay time to start eat audio, for more natural results
        IF i > 0 // if didn't pass X ms, wait a bit more before starting audio
            WAIT i
        ENDIF
        IF DOES_CHAR_EXIST hChar
            REPORT_MISSION_AUDIO_EVENT_AT_CHAR hChar SOUND_RESTAURANT_CJ_EAT
        ENDIF
    ELSE
        TASK_PLAY_ANIM_SECONDARY hChar $tAnim $tIFP 3.0 FALSE FALSE FALSE FALSE -1
    ENDIF

    // Wait animation
    timera = 0
    fAnimProgress = 0.0
    WHILE fAnimProgress < 1.0
        WAIT 0
        IF NOT DOES_CHAR_EXIST hChar
        OR NOT IS_CHAR_PLAYING_ANIM hChar $tAnim
        OR timera > 7000 //avoid soft lock
            BREAK
        ENDIF
        GET_CHAR_ANIM_CURRENT_TIME hChar $tAnim fAnimProgress
        IF fAnimProgress >= fFinishAnimTime
        AND bConsumed = FALSE

            IF NOT fHealth = -1.0
                GET_CHAR_HEALTH hChar iHealthBeforeEat
            ENDIF

            GOSUB PatchHungry
            // Update stats
            INCREMENT_INT_STAT 200 1 //STAT_NUMBER_OF_MEALS_EATEN
            i =# fCalories
            INCREMENT_INT_STAT 245 i //STAT_CALORIES
            GOSUB UnPatchHungry
            
            // Set char health, overwriting STAT_CALORIES effect
            IF NOT fHealth = -1.0
                GET_CHAR_HEALTH hChar i
                f =# iHealthBeforeEat
                f += fHealth
                GET_CHAR_MAX_HEALTH hChar fMaxHealth
                IF f > fMaxHealth
                    f = fMaxHealth
                ENDIF
                i =# f
                SET_CHAR_HEALTH hChar i
            ENDIF

            // not used yet: fThirst fDrunkness

            bConsumed = TRUE
            GOSUB HideObject
        ENDIF
    ENDWHILE

    GET_PED_TYPE hChar i
    IF i < 2 // is player
        SET_PLAYER_CYCLE_WEAPON_BUTTON i ON
    ENDIF

    WAIT 300

    IF bConsumed = TRUE
        GOSUB DecreaseCount // it will decrease 1 item count next frame
    ENDIF

    REMOVE_ANIMATION $tIFP
    IF NOT iAudioSlot = -1
	    CLEAR_MISSION_AUDIO iAudioSlot
    ENDIF

    GOSUB StopUsing
    TERMINATE_THIS_CUSTOM_SCRIPT

    //-------------------------------------------

    FindUnusedAudioSlot:
    // not working
    // workaround: don't play audio if is on mission, avoiding mission audio bugs
    IF IS_ON_MISSION
        iAudioSlot = -1
    ELSE
        iAudioSlot = 4
    ENDIF
    /*
    WHILE iAudioSlot > 0
        IF NOT HAS_MISSION_AUDIO_LOADED iAudioSlot
            RETURN
        ENDIF
        --iAudioSlot
    ENDWHILE
    iAudioSlot = -1
    */
    RETURN

    PatchHungry:
    bHungryMaxHealthPatched = FALSE
    IF NOT fHungryMult = 5.0 // don't patch if default
        READ_MEMORY 0x55C298 4 TRUE (pDefaultHungryMultPointer) //store default hungry pointer (maybe other mod change it)
        READ_MEMORY pDefaultHungryMultPointer 4 TRUE (f)
        // Test if value is valid, if not, it's because some mod hooked this address
        IF f > -10000.0
        AND f < 10000.0
            fPatchedHungryMult = fHungryMult * 0.1 // 5 will be 0.5 (default)
            GET_VAR_POINTER fPatchedHungryMult (i)
            WRITE_MEMORY 0x55C298 4 i TRUE
            //PRINT_FORMATTED_NOW "%f to %f" 1000 f fPatchedHungryMult
        ELSE
            // don't unpatch it
            pDefaultHungryMultPointer = 0
        ENDIF
        // don't restore hungry if health if full
        READ_MEMORY 0x55C31D 1 TRUE (i)
        IF i = 0x66 // is default (not patched by other mod)
            MAKE_NOP 0x55C31D 9
            bHungryMaxHealthPatched = TRUE
        ENDIF
    ENDIF
    RETURN

    UnPatchHungry:
    IF NOT pDefaultHungryMultPointer = 0
        WRITE_MEMORY 0x55C298 4 pDefaultHungryMultPointer TRUE
    ENDIF
    IF bHungryMaxHealthPatched = TRUE
        GET_LABEL_POINTER Original0x55C31D (i)
        COPY_MEMORY i 0x55C31D 9
        bHungryMaxHealthPatched = FALSE
    ENDIF
    RETURN

    //-------------------------------------------

    HideObject:
    READ_STRUCT_PARAM pStoredItem STORED_FLAGS i
    SET_LOCAL_VAR_BIT_CONST i ITEM_FLAG_HIDE
    WRITE_STRUCT_PARAM pStoredItem STORED_FLAGS i
    RETURN

    DecreaseCount:
    READ_STRUCT_PARAM pStoredItem STORED_FLAGS i
    SET_LOCAL_VAR_BIT_CONST i ITEM_FLAG_DECREASE_COUNT
    WRITE_STRUCT_PARAM pStoredItem STORED_FLAGS i
    RETURN

    StopUsing:
    READ_STRUCT_PARAM pStoredItem STORED_FLAGS i
    CLEAR_LOCAL_VAR_BIT_CONST i ITEM_FLAG_USING
    WRITE_STRUCT_PARAM pStoredItem STORED_FLAGS i
    RETURN
}
SCRIPT_END

Original0x55C31D:
DUMP
66 C7 86 DC CE B7 00 00 00
ENDDUMP
