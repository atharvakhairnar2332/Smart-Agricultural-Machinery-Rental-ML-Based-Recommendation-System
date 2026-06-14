from sqlalchemy import Column, Integer, String
from database import Base
from sqlalchemy import Float


class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(100),nullable=False)
    phone = Column(String(15), unique=True,nullable=False)
    password = Column(String(100),nullable=False)
    
    country = Column(String(100),nullable=False)
    state = Column(String(100),nullable=False)
    city = Column(String(100),nullable=False)
    email = Column(String(150), nullable=False,unique=True)



class Machine(Base):
    __tablename__ = "machines"

    id = Column(Integer, primary_key=True, index=True)

    type = Column(String(50),nullable=False)
    model_name = Column(String(100),nullable=False)

    hp_range = Column(Integer, nullable=True)
    cutting_width = Column(Float, nullable=True)
    working_width = Column(Float, nullable=True)
    row_count = Column(Integer, nullable=True)

    price_per_hour = Column(Integer,nullable=True)
    price_per_day = Column(Integer,nullable=True)
    price_per_week = Column(Integer,nullable=True)
    price_per_month = Column(Integer,nullable=True)

    owner_id = Column(Integer, nullable=True)

    image_url = Column(String, nullable=True)



class MachineRequest(Base):

    __tablename__ = "machine_requests"

    id = Column(Integer, primary_key=True, index=True)

    machine_id = Column(Integer, nullable=False)

    requester_id = Column(Integer, nullable=True)

    owner_id = Column(Integer, nullable=True)

    status = Column(String(20), default="pending")
