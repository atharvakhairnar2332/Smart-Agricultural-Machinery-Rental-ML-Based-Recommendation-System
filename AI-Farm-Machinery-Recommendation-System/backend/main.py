import pickle
from fastapi import HTTPException
from sqlalchemy import func
from Security import (
    hash_password,
    verify_password
)
from typing import Optional
from sqlalchemy import Column, Integer, String
from fastapi.staticfiles import StaticFiles

with open("Models/TractorModel.pkl", "rb") as f:
    tractor_model = pickle.load(f)


with open("Models/HarvestorModel.pkl", "rb") as f:
    harvester_model = pickle.load(f)


with open("Models/SeeddrillModel.pkl", "rb") as f:
    seed_drill_model = pickle.load(f)


with open("Models/RotavatorModel.pkl", "rb") as f:
    rotavator_model = pickle.load(f)

print("All ML models loaded ")


with open("Models/tractor_encoders.pkl", "rb") as f:
    tractor_encoders = pickle.load(f)


with open("Models/seeddrill_encoders.pkl", "rb") as f:
    seed_encoders = pickle.load(f)


with open("Models/rotavator_encoders.pkl", "rb") as f:
    rota_encoders = pickle.load(f)


with open("Models/harvester_encoders.pkl", "rb") as f:
    harv_encoders = pickle.load(f)

    print("All encoders loaded ")

from fastapi import FastAPI, Depends
from sqlalchemy.orm import Session
from typing import Dict
from pydantic import BaseModel

class RecommendRequest(BaseModel):
    type: str
    inputs: dict
    pricing_type: str

from database import SessionLocal, engine
import models
from models import Machine, User,MachineRequest

models.Base.metadata.create_all(bind=engine)

app = FastAPI()
app.mount("/uploads", StaticFiles(directory="uploads"), name="uploads")


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


@app.get("/")
def home():
    return {"message": "Backend + DB connected "}


class MachineCreate(BaseModel):
    type: str
    model_name: str

    hp_range: int | None = None
    working_width: float | None = None
    cutting_width: float | None = None
    row_count: int | None = None
   
    price_per_hour: int | None = None
    price_per_day: int | None = None
    price_per_week: int | None = None
    price_per_month: int | None = None

    owner_name: str
    owner_phone: str
    location: str
    owner_email: str
    image_url: Optional[str] = None


class UserCreate(BaseModel):
    name: str
    phone: str
    password: str
    country: str
    state: str
    city: str
    email: str

from pydantic import BaseModel

class UpdateProfileRequest(BaseModel):

    phone: str

    name: str

    email: str

    country: str

    state: str

    city: str


class UserLogin(BaseModel):
    phone: str
    password: str

class ChangePasswordRequest(BaseModel):

    phone: str

    current_password: str

    new_password: str



@app.post("/machines/add")
def add_machine(data: MachineCreate, db: Session = Depends(get_db)):

    user = db.query(User).filter(
        User.phone == data.owner_phone
    ).first()

    if not user:
        return {
            "message": "Owner not found"
        }

    machine = Machine(
    type=data.type,
    model_name=data.model_name,

    hp_range=data.hp_range,
    cutting_width=data.cutting_width,
    working_width=data.working_width,
    row_count=data.row_count,

    price_per_hour=data.price_per_hour,
    price_per_day=data.price_per_day,
    price_per_week=data.price_per_week,
    price_per_month=data.price_per_month,

    owner_id=user.id,

    image_url=data.image_url
)

    db.add(machine)
    db.commit()
    db.refresh(machine)

    return {"message": "Machine added successfully"}

from sqlalchemy import func
import re


@app.post("/recommend")
def recommend(data: RecommendRequest, db: Session = Depends(get_db)):

    import re

    
    machine_type = data.type.strip().lower()
    pricing_type = data.pricing_type.strip().lower()

    inputs = data.inputs if isinstance(data.inputs, dict) else data.inputs.dict()

    for k, v in inputs.items():
        if isinstance(v, str):
            inputs[k] = v.strip().lower()

    
    if machine_type == "tractor":

        prediction = tractor_model.predict([[
            float(inputs["farm_size"]),
            tractor_encoders["operation"].transform([inputs["operation"].capitalize()])[0],
            tractor_encoders["soil"].transform([inputs["soil_type"].capitalize()])[0],
            tractor_encoders["budget"].transform([inputs["budget"].capitalize()])[0]
        ]])[0]

        hp_range = tractor_encoders["target"].inverse_transform([prediction])[0]

        nums = re.findall(r"\d+", hp_range)
        min_hp, max_hp = int(nums[0]), int(nums[1])
        target_hp = (min_hp + max_hp) / 2

        all_machines = db.query(Machine).filter(Machine.type.ilike("tractor")).all()

        results = [
            m for m in all_machines
            if m.hp_range is not None and abs(m.hp_range - target_hp) <= 10
        ]

        final_prediction = hp_range

    
    elif machine_type == "seed drill":

        prediction = seed_drill_model.predict([[
            float(inputs["farm_size"]),
            seed_encoders["crop"].transform([inputs["crop_type"].capitalize()])[0],
            seed_encoders["budget"].transform([inputs["budget"].capitalize()])[0]
        ]])[0]

        all_machines = db.query(Machine).filter(Machine.type.ilike("seed drill")).all()

        results = [
            m for m in all_machines
            if m.row_count is not None and m.row_count == int(prediction)
        ]

        final_prediction = int(prediction)

    
    elif machine_type == "rotavator":

        prediction = rotavator_model.predict([[
            float(inputs["tractor_hp"]),
            rota_encoders["soil"].transform([inputs["soil_type"].capitalize()])[0],
            rota_encoders["budget"].transform([inputs["budget"].capitalize()])[0]
        ]])[0]

        width_range = rota_encoders["target"].inverse_transform([prediction])[0]

        nums = re.findall(r"\d+\.?\d*", width_range)
        min_w, max_w = float(nums[0]), float(nums[1])
        target_width = (min_w + max_w) / 2

        all_machines = db.query(Machine).filter(Machine.type.ilike("rotavator")).all()

        results = [
            m for m in all_machines
            if m.working_width is not None and abs(m.working_width - target_width) <= 1
        ]

        final_prediction = width_range

    
    elif machine_type == "harvester":

        prediction = harvester_model.predict([[
            float(inputs["farm_size"]),
            harv_encoders["crop"].transform([inputs["crop_type"].capitalize()])[0],
            harv_encoders["budget"].transform([inputs["budget"].capitalize()])[0]
        ]])[0]

        cut_range = harv_encoders["target"].inverse_transform([prediction])[0]

        nums = re.findall(r"\d+\.?\d*", cut_range)
        min_w, max_w = float(nums[0]), float(nums[1])
        target_width = (min_w + max_w) / 2

        all_machines = db.query(Machine).filter(Machine.type.ilike("harvester")).all()

        results = [
            m for m in all_machines
            if m.cutting_width is not None and abs(m.cutting_width - target_width) <= 2
        ]

        final_prediction = cut_range

    else:
        return {"error": "Invalid machine type"}

   
    budget_clean = inputs["budget"]
    filtered_results = []
    recommended_ids = set()

    for m in results:

        if pricing_type == "hour":
            price = m.price_per_hour
        elif pricing_type == "day":
            price = m.price_per_day
        elif pricing_type == "week":
            price = m.price_per_week
        else:
            price = m.price_per_month

        if price is None:
            continue

        price = int(price)

        if budget_clean == "low" and price <= 2000:
            filtered_results.append(m)
            recommended_ids.add(m.id)
        elif budget_clean == "medium" and 2000 <= price <= 5000:
            filtered_results.append(m)
            recommended_ids.add(m.id)
        elif budget_clean == "high" and price >= 5000:
            filtered_results.append(m)
            recommended_ids.add(m.id)

  
    return {
        "prediction": final_prediction,
        "count": len(filtered_results),
        "machines": [
            {
    "id": m.id,
    "type": m.type,
    "model_name": m.model_name,
    "hp_range": m.hp_range,
    "cutting_width": m.cutting_width,
    "working_width": m.working_width,
    "row_count": m.row_count,

    "price_per_hour": m.price_per_hour,
    "price_per_day": m.price_per_day,
    "price_per_week": m.price_per_week,
    "price_per_month": m.price_per_month,

    "owner_name":
        db.query(User).filter(
            User.id == m.owner_id
        ).first().name
        if m.owner_id and db.query(User).filter(
            User.id == m.owner_id
        ).first()
        else "",

    "owner_phone":
        db.query(User).filter(
            User.id == m.owner_id
        ).first().phone
        if m.owner_id and db.query(User).filter(
            User.id == m.owner_id
        ).first()
        else "",

    "owner_email":
        db.query(User).filter(
            User.id == m.owner_id
        ).first().email
        if m.owner_id and db.query(User).filter(
            User.id == m.owner_id
        ).first()
        else "",

    "location":
        db.query(User).filter(
            User.id == m.owner_id
        ).first().city
        if m.owner_id and db.query(User).filter(
            User.id == m.owner_id
        ).first()
        else "",

    "recommended": m.id in recommended_ids,

    "image_url": m.image_url
}
            for m in all_machines
        ]
    }


@app.post("/register")
def register(user: UserCreate, db: Session = Depends(get_db)):

    existing_user = db.query(User).filter(
        User.phone == user.phone
    ).first()

    if existing_user:
     raise HTTPException(
        status_code=400,
        detail="Phone already registered"
    )

    existing_email = db.query(User).filter(
    User.email == user.email
    ).first()

    if existing_email:
     raise HTTPException(
        status_code=400,
        detail="Email already registered"
    )

    new_user = User(
        name=user.name,
        phone=user.phone,
        password=hash_password(
        user.password
    ),
        country=user.country,
        state=user.state,
        city=user.city,
        email=user.email
    )

    db.add(new_user)
    db.commit()
    db.refresh(new_user)

    return {
        "message": "User registered successfully"
    }


@app.post("/login")
def login(user: UserLogin, db: Session = Depends(get_db)):

    existing_user = db.query(User).filter(
        User.phone == user.phone,
    ).first()

    if not existing_user:
      raise HTTPException(
         status_code=401,
         detail="Invalid credentials"
    )

    if not verify_password(
         user.password,
         existing_user.password
    ):
         raise HTTPException(
           status_code=401,
           detail="Invalid credentials"
    )

    return {
        "message": "Login successful",
        "name": existing_user.name,
        "phone": existing_user.phone,
        "country": existing_user.country,
        "state": existing_user.state,
        "city": existing_user.city,
        "email": existing_user.email
    }


@app.get("/my-machines/{phone}")
def get_my_machines(
    phone: str,
    db: Session = Depends(get_db)
):

    user = db.query(User).filter(
        User.phone == phone
    ).first()

    if not user:
        return {"machines": []}

    machines = db.query(Machine).filter(
    Machine.owner_id == user.id
    ).order_by(
    Machine.id.desc()
    ).all()

    return {
        "machines": [
            {
                "id": m.id,
                "type": m.type,
                "model_name": m.model_name,
                "hp_range": m.hp_range,
                "cutting_width": m.cutting_width,
                "working_width": m.working_width,
                "row_count": m.row_count,

                "price_per_hour": m.price_per_hour,
                "price_per_day": m.price_per_day,
                "price_per_week": m.price_per_week,
                "price_per_month": m.price_per_month,

                "location": user.city,
                "image_url": m.image_url
            }
            for m in machines
        ]
    }


@app.put("/machines/update/{machine_id}")
def update_machine(
    machine_id: int,
    data: MachineCreate,
    db: Session = Depends(get_db)
):

    machine = db.query(Machine).filter(
        Machine.id == machine_id
    ).first()

    if not machine:
        return {"error": "Machine not found"}

    machine.model_name = data.model_name

    machine.hp_range = data.hp_range
    machine.cutting_width = data.cutting_width
    machine.working_width = data.working_width
    machine.row_count = data.row_count

    machine.price_per_hour = data.price_per_hour
    machine.price_per_day = data.price_per_day
    machine.price_per_week = data.price_per_week
    machine.price_per_month = data.price_per_month
    machine.image_url = data.image_url

    db.commit()

    return {
        "message": "Machine updated successfully"
    }

from fastapi import File, UploadFile


@app.post("/upload-image")
async def upload_image(file: UploadFile = File(...)):

    file_path = f"uploads/{file.filename}"

    with open(file_path, "wb") as buffer:
        buffer.write(await file.read())

    return {
        "image_url": f"/uploads/{file.filename}"
    }


@app.post("/request-machine")
def request_machine(
    data: dict,
    db: Session = Depends(get_db)
):

    machine = db.query(Machine).filter(
        Machine.id == data["machine_id"]
    ).first()

    if not machine:
        return {
            "message": "Machine delisted"
        }

    user = db.query(User).filter(
        User.phone == data["requester_phone"]
    ).first()

    if not user:
        return {
            "message": "User not found"
        }

    request = MachineRequest(

        machine_id=data["machine_id"],

        requester_id=user.id,

        owner_id=machine.owner_id,

        status="pending"
    )

    db.add(request)

    db.commit()

    return {
        "message": "Request sent"
    }


@app.get("/notifications/{phone}")
def get_notifications(phone: str, db: Session = Depends(get_db)):

    owner = db.query(User).filter(
        User.phone == phone
    ).first()

    if not owner:
        return []

    requests = db.query(MachineRequest).filter(
        MachineRequest.owner_id == owner.id
    ).all()

    result = []

    for req in requests:

        requester = db.query(User).filter(
            User.id == req.requester_id
        ).first()

        machine = db.query(Machine).filter(
            Machine.id == req.machine_id
        ).first()

        result.append({

            "id": req.id,

            "machine_id": req.machine_id,

            "requester_name":
                requester.name if requester else "",

            "requester_phone":
                requester.phone if requester else "",

            "requester_email":
                requester.email if requester else "",

            "requester_location":
                requester.city if requester else "",

            "owner_phone":
                owner.phone,

           "type": machine.type if machine else "",

           "hp_range": machine.hp_range if machine else None,

            "cutting_width": machine.cutting_width if machine else None,

            "working_width": machine.working_width if machine else None,

            "row_count": machine.row_count if machine else None,

            "status":
                req.status,

            "machine_name":
                machine.model_name if machine else "",

            "price_per_hour":
                machine.price_per_hour if machine and machine.price_per_hour is not None else 0,

            "price_per_day":
                machine.price_per_day if machine and machine.price_per_day is not None else 0,

            "price_per_week":
                machine.price_per_week if machine and machine.price_per_week is not None else 0,

            "price_per_month":
                machine.price_per_month if machine and machine.price_per_month is not None else 0
        })

    return result


@app.put("/request-status/{request_id}")
def update_request_status(
    request_id: int,
    data: dict,
    db: Session = Depends(get_db)
):

    request = db.query(MachineRequest).filter(
        MachineRequest.id == request_id
    ).first()

    if not request:
        return {
            "error": "Request not found"
        }

    request.status = data["status"]

    db.commit()

    return {
        "message": "Status updated"
    }


@app.delete("/undo-request/{machine_id}/{phone}")
def undo_request(
    machine_id: int,
    phone: str,
    db: Session = Depends(get_db)
):

    user = db.query(User).filter(
        User.phone == phone
    ).first()

    if not user:
        return {
            "message": "User not found"
        }

    request = db.query(MachineRequest).filter(

        MachineRequest.machine_id == machine_id,

        MachineRequest.requester_id == user.id

    ).first()

    if request:

        db.delete(request)

        db.commit()

    return {
        "message": "Request removed"
    }


@app.get("/my-requests/{phone}")
def my_requests(phone: str, db: Session = Depends(get_db)):

    user = db.query(User).filter(
        User.phone == phone
    ).first()

    if not user:
        return []

    requests = db.query(MachineRequest).filter(
    MachineRequest.requester_id == user.id
    ).order_by(
    MachineRequest.id.desc()
    ).all()

    result = []

    for req in requests:

        machine = db.query(Machine).filter(
            Machine.id == req.machine_id
        ).first()

        owner = db.query(User).filter(
            User.id == req.owner_id
        ).first()

        result.append({

            "id": req.id,

            "machine_id": req.machine_id,

            "status": req.status,

            "machine_name":
                machine.model_name if machine else "",

            "owner_name":
                owner.name if owner else "",

            "owner_phone":
                owner.phone if owner else "",

            "owner_email":
                owner.email if owner else "",

            "location":
                owner.city if owner else "",

            "price_per_hour":
                machine.price_per_hour if machine and machine.price_per_hour is not None else 0,

            "price_per_day":
                machine.price_per_day if machine and machine.price_per_day is not None else 0,

            "type": machine.type if machine else "",

            "hp_range": machine.hp_range if machine else None,

            "cutting_width": machine.cutting_width if machine else None,

            "working_width": machine.working_width if machine else None,

            "row_count": machine.row_count if machine else None,

            "price_per_week":
                machine.price_per_week if machine and machine.price_per_week is not None else 0,

            "price_per_month":
                machine.price_per_month if machine and machine.price_per_month is not None else 0
        })

    return result


@app.delete("/machine/{machine_id}")
def delete_machine(

    machine_id: int,

    db: Session = Depends(get_db)
):

    machine = db.query(Machine).filter(
        Machine.id == machine_id
    ).first()

    if not machine:

        return {
            "message": "Machine not found"
        }

    db.delete(machine)

    db.commit()

    return {
        "message": "Machine deleted"
    }


@app.put("/update-profile")
def update_profile(
    data: UpdateProfileRequest,
    db: Session = Depends(get_db)
):

    user = db.query(User).filter(
        User.phone == data.phone
    ).first()

    if not user:

        return {
            "message": "User not found"
        }

    existing_email = db.query(User).filter(
        User.email == data.email,
        User.id != user.id
    ).first()

    if existing_email:

        raise HTTPException(
        status_code=400,
        detail="Email already registered"
    )

    user.name = data.name
    user.email = data.email
    user.country = data.country
    user.state = data.state
    user.city = data.city

    db.commit()

    return {
        "message": "Profile updated successfully"
    }


@app.put("/change-password")
def change_password(
    data: ChangePasswordRequest,
    db: Session = Depends(get_db)
):

    user = db.query(User).filter(
        User.phone == data.phone
    ).first()

    if not user:
        return {
            "message": "User not found"
        }

    if not verify_password(
      data.current_password,
      user.password
    ):
      raise HTTPException(
         status_code=400,
         detail="Current password is incorrect"
    )

    user.password = hash_password(
    data.new_password
    )

    db.commit()

    return {
        "message": "Password changed successfully"
    }
