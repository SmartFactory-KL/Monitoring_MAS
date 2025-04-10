package helper;

public enum ActionState {
	OPEN {
        @Override
        public ActionState start() {
            return EXECUTING;
        }
        
        @Override
        public ActionState abort() {
            return ABORTED;
        }
        
        @Override
        public ActionState error() {
            return ERROR;
        }
    },
	EXECUTING {
        @Override
        public ActionState suspend() {
            return SUSPENDED;
        }
        
        @Override
        public ActionState complete() {
            return COMPLETED;
        }
        
        @Override
        public ActionState abort() {
            return ABORTED;
        }
        
        @Override
        public ActionState error() {
            return ERROR;
        }
    },
	SUSPENDED {
        @Override
        public ActionState resume() {
            return EXECUTING;
        }
        
        @Override
        public ActionState reset() {
            return OPEN;
        }
        
        @Override
        public ActionState abort() {
            return ABORTED;
        }
        
        @Override
        public ActionState error() {
            return ERROR;
        }
    },
	COMPLETED,
	ABORTED,
	ERROR {
        @Override
        public ActionState recover() {
            return OPEN;
        }
    };

	public ActionState start() {
        invalidTransition("start");
        return this;
    }

	public ActionState suspend() {
        invalidTransition("pause");
        return this;
    }

	public ActionState resume() {
        invalidTransition("resume");
        return this;
    }

	public ActionState reset() {
        invalidTransition("reset");
        return this;
    }

	public ActionState abort() {
        invalidTransition("abort");
        return this;
    }

	public ActionState error() {
        invalidTransition("error");
        return this;
    }

	public ActionState complete() {
        invalidTransition("complete");
        return this;
    }

	public ActionState recover() {
        invalidTransition("recover");
        return this;
    }
	
    // Helper to handle invalid transitions
    private void invalidTransition(String action) {
//        System.out.println("Invalid action '" + action + "' for state: " + this);
    }
}
