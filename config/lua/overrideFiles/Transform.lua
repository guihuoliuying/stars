Transform = {}
function Transform:TransformVector(x, y, z) end
function Transform:InverseTransformVector(x, y, z) end
function Transform:TransformPoint(x, y, z) end
function Transform:InverseTransformPoint(x, y, z) end
function Transform:DetachChildren() end
function Transform:SetAsFirstSibling() end
function Transform:SetAsLastSibling() end
function Transform:SetSiblingIndex(index) end
function Transform:GetSiblingIndex() end
function Transform:Find(name) end
function Transform:IsChildOf(parent) end
function Transform:FindChild(name) end
function Transform:GetEnumerator() end
function Transform:RotateAround(point, axis, angle) end
function Transform:RotateAroundLocal(axis, angle) end
function Transform:GetChild(index) end
function Transform:GetChildCount() end
function Transform:SetParent(parent, worldPositionStays) end
function Transform:Translate(x, y, z, relativeTo) end
function Transform:Rotate(xAngle, yAngle, zAngle, relativeTo) end
function Transform:LookAt(target, worldUp) end
function Transform:TransformDirection(x, y, z) end
function Transform:InverseTransformDirection(x, y, z) end
function Transform:GetComponent(type) end
function Transform:GetComponentInChildren(t, includeInactive) end
function Transform:GetComponentsInChildren(t, includeInactive) end
function Transform:GetComponentInParent(t) end
function Transform:GetComponentsInParent(t, includeInactive) end
function Transform:GetComponents(type, results) end
function Transform:CompareTag(tag) end
function Transform:SendMessageUpwards(methodName, value, options) end
function Transform:SendMessage(methodName, value, options) end
function Transform:BroadcastMessage(methodName, parameter, options) end
function Transform:ToString() end
function Transform:Equals(o) end
function Transform:GetHashCode() end
function Transform:GetInstanceID() end
function Transform:GetType() end
