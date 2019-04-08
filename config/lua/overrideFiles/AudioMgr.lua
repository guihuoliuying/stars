AudioMgr = {}
function AudioMgr.init() end
function AudioMgr.isValid() end
function AudioMgr.setEnable(value_) end
function AudioMgr.PlaySoundInfoNpc(soundInfoId_) end
function AudioMgr.PlaySoundInfoUI(soundInfoId_) end
function AudioMgr.PlaySoundInfoFight(soundInfoId_) end
function AudioMgr.PlaySoundInfoScene(soundInfoId_) end
function AudioMgr.PlaySoundInfo(soundInfoId_, audiotype_) end
function AudioMgr.PlayUI(soundId, delaySeconds, loopCount) end
function AudioMgr.PlayFight(soundId, delaySeconds, loopCount) end
function AudioMgr.PlayScene(soundId, delaySeconds, loopCount, needFadeIn, needFadeOut, fadeInTime, fadeOutTime, playCallback) end
function AudioMgr.PlayBattle(soundId, delaySeconds, loopCount) end
function AudioMgr.PlayBattleScene( soundId, delaySeconds, loopCount, needFadeIn, needFadeOut, fadeInTime, fadeOutTime ) end
function AudioMgr.stopSoundGroupByType(audiotype, fadeOutTime, isAfterToPool, soundCalcType) end
function AudioMgr.stopById(soundId, fadeOutTime, isAfterToPool, soundCalcType, stopCompleteFunc_) end
function AudioMgr.stopBySoundInfo(soundInfoId, fadeOutTime, isAfterToPool, soundCalcType, stopCompleteFunc_) end
function AudioMgr.resumeSoundGroupByType(audiotype, fadeInTime) end
function AudioMgr.resumeById(soundId, fadeInTime) end
function AudioMgr.clearAllSound() end
function AudioMgr.clearAllBattleSound() end
function AudioMgr.dispose(isDisposeAll, isDisposePlaying) end
function AudioMgr.reset() end
function AudioMgr.Play(soundId, delaySeconds, audiotype, loopCount, needFadeIn, needFadeOut, fadeInTime, fadeOutTime, playCompleteFunc) end
function AudioMgr.getVolumnRateBySoundType(audiotype) end
function AudioMgr.isSoundPlaying(soundId) end
function AudioMgr.isSoundPlayingBySoundInfoId(soundInfoId) end
function AudioMgr.isMuteSoundType(audiotype) end
function AudioMgr.setMuteBySoundType(audiotype, isMute) end
function AudioMgr.setMuteAllSound(isMute) end
function AudioMgr.setVolumnRateBySoundType(audiotype, rate) end
