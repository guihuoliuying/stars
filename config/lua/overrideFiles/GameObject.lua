GameObject = {}
function GameObject.New(name, components) end
function GameObject.Find(name) end
function GameObject:SampleAnimation(clip, time) end
function GameObject:AddComponent(className) end
function GameObject:PlayAnimation(animation) end
function GameObject:StopAnimation() end
function GameObject.CreatePrimitive(type) end
function GameObject:GetComponent(type) end
function GameObject:GetComponentInChildren(type, includeInactive) end
function GameObject:GetComponentInParent(type) end
function GameObject:GetComponents(type, results) end
function GameObject:GetComponentsInChildren(type, includeInactive) end
function GameObject:GetComponentsInParent(type, includeInactive) end
function GameObject:SetActive(value) end
function GameObject:SetActiveRecursively(state) end
function GameObject:CompareTag(tag) end
function GameObject.FindGameObjectWithTag(tag) end
function GameObject.FindWithTag(tag) end
function GameObject.FindGameObjectsWithTag(tag) end
function GameObject:SendMessageUpwards(methodName, value, options) end
function GameObject:SendMessage(methodName, value, options) end
function GameObject:BroadcastMessage(methodName, parameter, options) end
function GameObject:ToString() end
function GameObject:Equals(o) end
function GameObject:GetHashCode() end
function GameObject:GetInstanceID() end
function GameObject:GetType() end
