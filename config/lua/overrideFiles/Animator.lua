Animator = {}
function Animator.New() end
function Animator:Update(deltaTime) end
function Animator:Rebind() end
function Animator:ApplyBuiltinRootMotion() end
function Animator:GetVector(name) end
function Animator:SetVector(name, value) end
function Animator:GetQuaternion(name) end
function Animator:SetQuaternion(name, value) end
function Animator:GetCurrentAnimationClipState(layerIndex) end
function Animator:GetNextAnimationClipState(layerIndex) end
function Animator:PlayInFixedTime(stateName, layer, fixedTime) end
function Animator:Play(stateName, layer, normalizedTime) end
function Animator:SetTarget(targetIndex, targetNormalizedTime) end
function Animator:IsControlled(transform) end
function Animator:GetBoneTransform(humanBoneId) end
function Animator:StartPlayback() end
function Animator:StopPlayback() end
function Animator:StartRecording(frameCount) end
function Animator:StopRecording() end
function Animator:HasState(layerIndex, stateID) end
function Animator.StringToHash(name) end
function Animator:SetIKRotationWeight(goal, value) end
function Animator:GetIKHintPosition(hint) end
function Animator:SetIKHintPosition(hint, hintPosition) end
function Animator:GetIKHintPositionWeight(hint) end
function Animator:SetIKHintPositionWeight(hint, value) end
function Animator:SetLookAtPosition(lookAtPosition) end
function Animator:SetLookAtWeight(weight, bodyWeight, headWeight, eyesWeight, clampWeight) end
function Animator:SetBoneLocalRotation(humanBoneId, rotation) end
function Animator:GetBehaviour() end
function Animator:GetBehaviours() end
function Animator:GetLayerName(layerIndex) end
function Animator:GetLayerIndex(layerName) end
function Animator:GetLayerWeight(layerIndex) end
function Animator:SetLayerWeight(layerIndex, weight) end
function Animator:GetCurrentAnimatorStateInfo(layerIndex) end
function Animator:GetNextAnimatorStateInfo(layerIndex) end
function Animator:GetAnimatorTransitionInfo(layerIndex) end
function Animator:GetCurrentAnimatorClipInfo(layerIndex) end
function Animator:GetNextAnimatorClipInfo(layerIndex) end
function Animator:IsInTransition(layerIndex) end
function Animator:GetParameter(index) end
function Animator:MatchTarget(matchPosition, matchRotation, targetBodyPart, weightMask, startNormalizedTime, targetNormalizedTime) end
function Animator:InterruptMatchTarget(completeMatch) end
function Animator:ForceStateNormalizedTime(normalizedTime) end
function Animator:CrossFadeInFixedTime(stateName, transitionDuration, layer, fixedTime) end
function Animator:CrossFade(stateName, transitionDuration, layer, normalizedTime) end
function Animator:GetFloat(name) end
function Animator:SetFloat(name, value, dampTime, deltaTime) end
function Animator:GetBool(name) end
function Animator:SetBool(name, value) end
function Animator:GetInteger(name) end
function Animator:SetInteger(name, value) end
function Animator:SetTrigger(name) end
function Animator:ResetTrigger(name) end
function Animator:IsParameterControlledByCurve(name) end
function Animator:GetIKPosition(goal) end
function Animator:SetIKPosition(goal, goalPosition) end
function Animator:GetIKRotation(goal) end
function Animator:SetIKRotation(goal, goalRotation) end
function Animator:GetIKPositionWeight(goal) end
function Animator:SetIKPositionWeight(goal, value) end
function Animator:GetIKRotationWeight(goal) end
function Animator:Stop() end
function Animator:SetTime(time) end
function Animator:GetTime() end
function Animator:SetTimeUpdateMode(mode) end
function Animator:GetTimeUpdateMode() end
function Animator:GetComponent(type) end
function Animator:GetComponentInChildren(t, includeInactive) end
function Animator:GetComponentsInChildren(t, includeInactive) end
function Animator:GetComponentInParent(t) end
function Animator:GetComponentsInParent(t, includeInactive) end
function Animator:GetComponents(type, results) end
function Animator:CompareTag(tag) end
function Animator:SendMessageUpwards(methodName, value, options) end
function Animator:SendMessage(methodName, value, options) end
function Animator:BroadcastMessage(methodName, parameter, options) end
function Animator:ToString() end
function Animator:Equals(o) end
function Animator:GetHashCode() end
function Animator:GetInstanceID() end
function Animator:GetType() end
